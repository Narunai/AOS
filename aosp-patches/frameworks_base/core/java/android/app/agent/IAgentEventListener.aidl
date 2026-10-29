/*
 * Copyright (C) 2026 The Android Open Source Project
 * Copyright (C) 2026 Agentic OS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.agent;

import android.app.agent.AgentContextSnapshot;

/**
 * Asynchronous callback interface for system agent event streams.
 * {@hide}
 */
oneway interface IAgentEventListener {
    /**
     * Called when the focused foreground activity changes.
     */
    void onActivityFocused(String packageName, String activityName, long timestampMs);

    /**
     * Called when a sanitized compact screen snapshot is ready.
     */
    void onScreenSnapshotAvailable(in AgentContextSnapshot snapshot);

    /**
     * Called when an incoming notification is posted from an unblocked app.
     */
    void onNotificationReceived(String packageName, String title, String text, long postTimeMs);
}
