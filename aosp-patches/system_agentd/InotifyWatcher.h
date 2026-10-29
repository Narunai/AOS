/*
 * Copyright (C) 2026 Agentic OS Project
 *
 * inotify filesystem watcher for whitelisted app databases.
 */

#pragma once

#include <string>
#include <vector>
#include <functional>
#include <unordered_map>
#include <sys/inotify.h>

namespace agentos {

using DbChangeEventCallback = std::function<void(const std::string& packageName, const std::string& dbPath)>;

class InotifyWatcher {
public:
    InotifyWatcher();
    ~InotifyWatcher();

    bool init();
    bool addWatchPackage(const std::string& packageName);
    void removeWatchPackage(const std::string& packageName);

    /**
     * Blocks on poll/read loop and calls callback when .db-wal files are updated.
     */
    void runLoop(const DbChangeEventCallback& callback);
    void stop();

private:
    int mInotifyFd = -1;
    bool mRunning = false;
    std::unordered_map<int, std::string> mWatchDescriptors; // wd -> package/dbPath
};

} // namespace agentos
