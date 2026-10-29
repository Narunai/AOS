/*
 * Copyright (C) 2026 Agentic OS Project
 *
 * Strict read-only, non-locking SQLite reader.
 */

#pragma once

#include <string>
#include <vector>
#include <sqlite3.h>

namespace agentos {

struct RowData {
    std::string tableName;
    std::vector<std::pair<std::string, std::string>> columns;
};

class WalSqliteReader {
public:
    WalSqliteReader();
    ~WalSqliteReader();

    /**
     * Opens database strictly with SQLITE_OPEN_READONLY | SQLITE_OPEN_URI and query_only pragma.
     * Guarantees no write locks or corruption of the foreground app database.
     */
    bool openReadOnly(const std::string& dbPath);

    /**
     * Reads recent rows from the specified table.
     */
    std::vector<RowData> queryRecent(const std::string& tableName, int limit = 10);

    void close();

private:
    sqlite3* mDb = nullptr;
    std::string mCurrentPath;
};

} // namespace agentos
