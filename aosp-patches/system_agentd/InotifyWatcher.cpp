/*
 * Copyright (C) 2026 Agentic OS Project
 *
 * inotify filesystem watcher for whitelisted app databases.
 */

#include "InotifyWatcher.h"
#include <unistd.h>
#include <poll.h>
#include <android/log.h>

#define LOG_TAG "agentd-inotify"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace agentos {

InotifyWatcher::InotifyWatcher() {}

InotifyWatcher::~InotifyWatcher() {
    stop();
}

bool InotifyWatcher::init() {
    mInotifyFd = inotify_init1(IN_NONBLOCK | IN_CLOEXEC);
    if (mInotifyFd < 0) {
        ALOGE("Failed to initialize inotify: %d", errno);
        return false;
    }
    mRunning = true;
    return true;
}

bool InotifyWatcher::addWatchPackage(const std::string& packageName) {
    if (mInotifyFd < 0) return false;

    std::string dbDir = "/data/data/" + packageName + "/databases";
    int wd = inotify_add_watch(mInotifyFd, dbDir.c_str(), IN_CLOSE_WRITE | IN_MOVED_TO);
    if (wd < 0) {
        ALOGW("Cannot add inotify watch for %s (may not have databases dir yet)", packageName.c_str());
        return false;
    }

    mWatchDescriptors[wd] = packageName;
    ALOGI("Added inotify watch for %s (wd=%d)", packageName.c_str(), wd);
    return true;
}

void InotifyWatcher::removeWatchPackage(const std::string& packageName) {
    for (auto it = mWatchDescriptors.begin(); it != mWatchDescriptors.end(); ) {
        if (it->second == packageName) {
            inotify_rm_watch(mInotifyFd, it->first);
            it = mWatchDescriptors.erase(it);
        } else {
            ++it;
        }
    }
}

void InotifyWatcher::runLoop(const DbChangeEventCallback& callback) {
    constexpr size_t BUF_LEN = 1024 * (sizeof(struct inotify_event) + 16);
    char buffer[BUF_LEN] __attribute__((aligned(__alignof__(struct inotify_event))));

    struct pollfd pfd;
    pfd.fd = mInotifyFd;
    pfd.events = POLLIN;

    while (mRunning) {
        int pollNum = poll(&pfd, 1, 3000); // 3 sec timeout
        if (pollNum < 0) {
            if (errno == EINTR) continue;
            break;
        }
        if (pollNum == 0) continue; // Timeout, loop back

        if (pfd.revents & POLLIN) {
            ssize_t len = read(mInotifyFd, buffer, BUF_LEN);
            if (len <= 0) continue;

            const struct inotify_event* event = nullptr;
            for (char* ptr = buffer; ptr < buffer + len; ptr += sizeof(struct inotify_event) + event->len) {
                event = reinterpret_cast<const struct inotify_event*>(ptr);
                if (event->len > 0) {
                    std::string fileName(event->name);
                    // Look for Write-Ahead Log (.db-wal) or SQLite DB files
                    if (fileName.find(".db") != std::string::npos) {
                        auto it = mWatchDescriptors.find(event->wd);
                        if (it != mWatchDescriptors.end()) {
                            std::string pkg = it->second;
                            std::string fullPath = "/data/data/" + pkg + "/databases/" + fileName;
                            callback(pkg, fullPath);
                        }
                    }
                }
            }
        }
    }
}

void InotifyWatcher::stop() {
    mRunning = false;
    if (mInotifyFd >= 0) {
        for (const auto& pair : mWatchDescriptors) {
            inotify_rm_watch(mInotifyFd, pair.first);
        }
        mWatchDescriptors.clear();
        close(mInotifyFd);
        mInotifyFd = -1;
    }
}

} // namespace agentos
