/*
 * Copyright (C) 2026 Agentic OS Project
 *
 * Strict read-only, non-locking SQLite reader.
 */

#include "WalSqliteReader.h"
#include <android/log.h>

#define LOG_TAG "agentd-sqlite"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace agentos {

WalSqliteReader::WalSqliteReader() {}

WalSqliteReader::~WalSqliteReader() {
    close();
}

bool WalSqliteReader::openReadOnly(const std::string& dbPath) {
    close();

    // Use URI filename with immutable=1 flag to ensure read-only snapshot isolation
    std::string uriPath = "file:" + dbPath + "?immutable=1";
    int flags = SQLITE_OPEN_READONLY | SQLITE_OPEN_URI;

    int rc = sqlite3_open_v2(uriPath.c_str(), &mDb, flags, nullptr);
    if (rc != SQLITE_OK) {
        ALOGE("Failed to open %s read-only: %s", dbPath.c_str(), sqlite3_errmsg(mDb));
        close();
        return false;
    }

    // Enforce query_only pragma
    char* err = nullptr;
    sqlite3_exec(mDb, "PRAGMA query_only = ON;", nullptr, nullptr, &err);
    if (err) {
        sqlite3_free(err);
    }

    mCurrentPath = dbPath;
    ALOGI("Successfully attached read-only snapshot to %s", dbPath.c_str());
    return true;
}

std::vector<RowData> WalSqliteReader::queryRecent(const std::string& tableName, int limit) {
    std::vector<RowData> results;
    if (!mDb) return results;

    std::string sql = "SELECT * FROM " + tableName + " ORDER BY rowid DESC LIMIT " + std::to_string(limit) + ";";
    sqlite3_stmt* stmt = nullptr;

    if (sqlite3_prepare_v2(mDb, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        ALOGE("Query failed on table %s: %s", tableName.c_str(), sqlite3_errmsg(mDb));
        return results;
    }

    int colCount = sqlite3_column_count(stmt);
    while (sqlite3_step(stmt) == SQLITE_ROW) {
        RowData row;
        row.tableName = tableName;
        for (int i = 0; i < colCount; i++) {
            const char* colName = sqlite3_column_name(stmt, i);
            const unsigned char* val = sqlite3_column_text(stmt, i);
            std::string colVal = val ? reinterpret_cast<const char*>(val) : "";
            row.columns.emplace_back(colName ? colName : "", colVal);
        }
        results.push_back(std::move(row));
    }

    sqlite3_finalize(stmt);
    return results;
}

void WalSqliteReader::close() {
    if (mDb) {
        sqlite3_close_v2(mDb);
        mDb = nullptr;
        mCurrentPath.clear();
    }
}

} // namespace agentos
