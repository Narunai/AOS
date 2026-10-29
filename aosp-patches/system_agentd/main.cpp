/*
 * Copyright (C) 2026 Agentic OS Project
 *
 * agentd - Native Context & SQLite Data Daemon
 */

#include <android/log.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <thread>
#include "WalSqliteReader.h"
#include "InotifyWatcher.h"

#define LOG_TAG "agentd"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

int main(int /*argc*/, char** /*argv*/) {
    ALOGI("Starting AgentOS Native Context Daemon (agentd)...");

    // Initialize inotify watcher
    agentos::InotifyWatcher watcher;
    if (!watcher.init()) {
        ALOGE("Failed to start inotify watcher, terminating agentd");
        return 1;
    }

    // Default monitored user-approved applications
    watcher.addWatchPackage("com.google.android.keep");
    watcher.addWatchPackage("com.android.chrome");

    // Start background file change listener thread
    std::thread watcherThread([&watcher]() {
        agentos::WalSqliteReader reader;
        watcher.runLoop([&reader](const std::string& pkg, const std::string& dbPath) {
            ALOGI("Detected DB change in %s: %s", pkg.c_str(), dbPath.c_str());
            // Safely open in immutable read-only mode without database locks
            if (reader.openReadOnly(dbPath)) {
                // Read top 3 recent updates
                // reader.queryRecent(...);
                reader.close();
            }
        });
    });

    // Start thread pool for Binder IPC
    ABinderProcess_setThreadPoolMaxThreadCount(4);
    ABinderProcess_startThreadPool();

    ALOGI("agentd ready. Joining IPC thread pool...");
    ABinderProcess_joinThreadPool();

    watcher.stop();
    if (watcherThread.joinable()) {
        watcherThread.join();
    }
    return 0;
}
