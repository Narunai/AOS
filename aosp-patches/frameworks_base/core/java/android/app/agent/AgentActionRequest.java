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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Encapsulates an automated action request dispatched through the Agent Context Bus.
 * {@hide}
 */
public final class AgentActionRequest implements Parcelable {

    public static final int ACTION_TYPE_CLICK = 1;
    public static final int ACTION_TYPE_INPUT_TEXT = 2;
    public static final int ACTION_TYPE_SCROLL = 3;
    public static final int ACTION_TYPE_LAUNCH_APP = 4;

    private final int mActionType;
    private final int mTargetNodeId;
    private final String mPayload;
    private final String mPackageName;
    private final boolean mRequiresUserConfirmation;

    public AgentActionRequest(
            int actionType,
            int targetNodeId,
            @Nullable String payload,
            @Nullable String packageName,
            boolean requiresUserConfirmation) {
        mActionType = actionType;
        mTargetNodeId = targetNodeId;
        mPayload = payload;
        mPackageName = packageName;
        mRequiresUserConfirmation = requiresUserConfirmation;
    }

    protected AgentActionRequest(Parcel in) {
        mActionType = in.readInt();
        mTargetNodeId = in.readInt();
        mPayload = in.readString();
        mPackageName = in.readString();
        mRequiresUserConfirmation = in.readByte() != 0;
    }

    public static final Creator<AgentActionRequest> CREATOR = new Creator<AgentActionRequest>() {
        @Override
        public AgentActionRequest createFromParcel(Parcel in) {
            return new AgentActionRequest(in);
        }

        @Override
        public AgentActionRequest[] newArray(int size) {
            return new AgentActionRequest[size];
        }
    };

    public int getActionType() { return mActionType; }
    public int getTargetNodeId() { return mTargetNodeId; }
    @Nullable public String getPayload() { return mPayload; }
    @Nullable public String getPackageName() { return mPackageName; }
    public boolean requiresUserConfirmation() { return mRequiresUserConfirmation; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mActionType);
        dest.writeInt(mTargetNodeId);
        dest.writeString(mPayload);
        dest.writeString(mPackageName);
        dest.writeByte((byte) (mRequiresUserConfirmation ? 1 : 0));
    }
}
