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
import android.app.agent.AgentActionRequest;
import android.app.agent.IAgentEventListener;

/**
 * Main System Service Interface for Agent Context Bus.
 * Protected by android.permission.ACCESS_AGENT_CONTEXT_BUS (signature|privileged).
 * {@hide}
 */
interface IAgentContextManager {

    /**
     * Retrieves the current privacy-sanitized screen snapshot directly from WindowManagerService.
     */
    AgentContextSnapshot getCurrentScreenSnapshot(int flags);

    /**
     * Subscribes to real-time events (Task Focus, Notifications, Semantic Screen Updates).
     */
    void registerEventListener(IAgentEventListener listener, in String[] packageFilter);

    /**
     * Unsubscribes an event listener.
     */
    void unregisterEventListener(IAgentEventListener listener);

    /**
     * Dispatches an action to be executed directly by the OS input subsystem.
     */
    boolean executeAction(in AgentActionRequest action);

    /**
     * Updates privacy observation policy for a specific package (0: Blocked, 1: On-Demand, 2: Full).
     */
    void setPackagePrivacyPolicy(String packageName, int policyMode);

    /**
     * Returns whether the global AI co-pilot kill-switch is active.
     */
    boolean isAgentGloballyPaused();

    /**
     * Sets the global AI co-pilot pause state.
     */
    void setAgentGloballyPaused(boolean paused);
}
