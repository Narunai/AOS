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

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemService;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

/**
 * Client class for interacting with the system-level Agent Context Bus.
 * Accessible via {@code context.getSystemService(AgentManager.class)}.
 */
@SystemService(Context.AGENT_SERVICE)
public final class AgentManager {

    private static final String TAG = "AgentManager";
    public static final String ACTION_AGENT_HOTKEY_TRIGGERED = "android.intent.action.AGENT_HOTKEY_TRIGGERED";

    private final Context mContext;
    private final IAgentContextManager mService;

    public AgentManager(@NonNull Context context, @NonNull IAgentContextManager service) {
        mContext = context;
        mService = service;
    }

    @Nullable
    @RequiresPermission("android.permission.ACCESS_AGENT_CONTEXT_BUS")
    public AgentContextSnapshot getCurrentScreenSnapshot() {
        try {
            return mService.getCurrentScreenSnapshot(0);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get current screen snapshot", e);
            return null;
        }
    }

    @RequiresPermission("android.permission.ACCESS_AGENT_CONTEXT_BUS")
    public void registerEventListener(
            @NonNull IAgentEventListener listener,
            @Nullable String[] packageFilter) {
        try {
            mService.registerEventListener(listener, packageFilter);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register event listener", e);
        }
    }

    @RequiresPermission("android.permission.ACCESS_AGENT_CONTEXT_BUS")
    public void unregisterEventListener(@NonNull IAgentEventListener listener) {
        try {
            mService.unregisterEventListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to unregister event listener", e);
        }
    }

    @RequiresPermission("android.permission.ACCESS_AGENT_CONTEXT_BUS")
    public boolean executeAction(@NonNull AgentActionRequest request) {
        try {
            return mService.executeAction(request);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to execute agent action", e);
            return false;
        }
    }

    @RequiresPermission("android.permission.ACCESS_AGENT_CONTEXT_BUS")
    public void setPackagePrivacyPolicy(@NonNull String packageName, int policyMode) {
        try {
            mService.setPackagePrivacyPolicy(packageName, policyMode);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set privacy policy for " + packageName, e);
        }
    }

    public boolean isAgentGloballyPaused() {
        try {
            return mService.isAgentGloballyPaused();
        } catch (RemoteException e) {
            return false;
        }
    }

    @RequiresPermission("android.permission.ACCESS_AGENT_CONTEXT_BUS")
    public void setAgentGloballyPaused(boolean paused) {
        try {
            mService.setAgentGloballyPaused(paused);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to update global pause state", e);
        }
    }
}
