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

package com.android.server.agent;

import android.app.agent.AgentActionRequest;
import android.app.agent.AgentContextSnapshot;
import android.app.agent.IAgentContextManager;
import android.app.agent.IAgentEventListener;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.SystemService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Core System Server implementation of Agent Context Bus.
 * Dispatches real-time structured view tree and lifecycle events to privileged agent handlers.
 */
public class AgentContextManagerService extends SystemService {

    private static final String TAG = "AgentContextManager";
    private static final String PERMISSION_AGENT_BUS = "android.permission.ACCESS_AGENT_CONTEXT_BUS";

    private final Context mContext;
    private final RemoteCallbackList<IAgentEventListener> mListeners = new RemoteCallbackList<>();
    private final ConcurrentHashMap<String, Integer> mPrivacyPolicies = new ConcurrentHashMap<>();
    private final ArraySet<String> mBankingBlacklist = new ArraySet<>();
    private volatile boolean mIsGloballyPaused = false;
    private volatile AgentContextSnapshot mLastSnapshot = null;

    public AgentContextManagerService(Context context) {
        super(context);
        mContext = context;
        initDefaultBlacklist();
    }

    private void initDefaultBlacklist() {
        // Built-in protection for financial institutions
        mBankingBlacklist.add("com.kasikorn.retail.mbanking.wap");
        mBankingBlacklist.add("com.scb.phone");
        mBankingBlacklist.add("ktbcs.netbank");
        mBankingBlacklist.add("com.bbl.mobilephone");
        mBankingBlacklist.add("com.krungsri.kma");
        mBankingBlacklist.add("com.ttbbank.oneapp");
        mBankingBlacklist.add("th.co.truemoney.wallet");
    }

    @Override
    public void onStart() {
        publishBinderService(Context.AGENT_SERVICE, mBinderImpl);
        Log.i(TAG, "AgentContextManagerService started and published to ServiceManager");
    }

    /**
     * Internal entry point called by ActivityTaskManagerService on foreground activity switch.
     */
    public void notifyActivityFocused(String packageName, String activityName) {
        if (mIsGloballyPaused || mBankingBlacklist.contains(packageName)) {
            return;
        }

        final long now = System.currentTimeMillis();
        final int n = mListeners.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                mListeners.getBroadcastItem(i).onActivityFocused(packageName, activityName, now);
            } catch (RemoteException ignored) {
            }
        }
        mListeners.finishBroadcast();
    }

    /**
     * Internal entry point called by WindowManagerService when a new semantic screen tree is captured.
     */
    public void notifyScreenSnapshot(AgentContextSnapshot snapshot) {
        if (mIsGloballyPaused || mBankingBlacklist.contains(snapshot.getPackageName())) {
            return;
        }

        mLastSnapshot = snapshot;
        final int n = mListeners.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                mListeners.getBroadcastItem(i).onScreenSnapshotAvailable(snapshot);
            } catch (RemoteException ignored) {
            }
        }
        mListeners.finishBroadcast();
    }

    private final IAgentContextManager.Stub mBinderImpl = new IAgentContextManager.Stub() {

        @Override
        public AgentContextSnapshot getCurrentScreenSnapshot(int flags) {
            enforceCallingPermission();
            if (mIsGloballyPaused) return null;
            return mLastSnapshot;
        }

        @Override
        public void registerEventListener(IAgentEventListener listener, String[] packageFilter) {
            enforceCallingPermission();
            if (listener != null) {
                mListeners.register(listener);
            }
        }

        @Override
        public void unregisterEventListener(IAgentEventListener listener) {
            enforceCallingPermission();
            if (listener != null) {
                mListeners.unregister(listener);
            }
        }

        @Override
        public boolean executeAction(AgentActionRequest action) {
            enforceCallingPermission();
            if (mIsGloballyPaused || action == null) return false;

            Log.i(TAG, "Executing agent action type: " + action.getActionType() + " on target: " + action.getTargetNodeId());
            // In AOSP, dispatches through InputManagerInternal / ActivityTaskManagerInternal
            return true;
        }

        @Override
        public void setPackagePrivacyPolicy(String packageName, int policyMode) {
            enforceCallingPermission();
            if (packageName != null) {
                mPrivacyPolicies.put(packageName, policyMode);
            }
        }

        @Override
        public boolean isAgentGloballyPaused() {
            return mIsGloballyPaused;
        }

        @Override
        public void setAgentGloballyPaused(boolean paused) {
            enforceCallingPermission();
            mIsGloballyPaused = paused;
            Log.i(TAG, "Global Agent pause state updated to: " + paused);
        }

        private void enforceCallingPermission() {
            mContext.enforceCallingOrSelfPermission(
                    PERMISSION_AGENT_BUS,
                    "Must hold " + PERMISSION_AGENT_BUS + " to access Agent Context Bus"
            );
        }
    };
}
