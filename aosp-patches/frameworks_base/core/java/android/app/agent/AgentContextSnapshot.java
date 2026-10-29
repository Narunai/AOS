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
 * Encapsulates a privacy-sanitized semantic snapshot of the active screen.
 * {@hide}
 */
public final class AgentContextSnapshot implements Parcelable {

    private final String mSnapshotId;
    private final long mTimestampMs;
    private final String mPackageName;
    private final String mActivityName;
    private final String mWindowTitle;
    private final String mContentHash;
    private final String mCompactDomJson;
    private final String mRawExtractedText;

    public AgentContextSnapshot(
            @NonNull String snapshotId,
            long timestampMs,
            @NonNull String packageName,
            @Nullable String activityName,
            @Nullable String windowTitle,
            @NonNull String contentHash,
            @NonNull String compactDomJson,
            @NonNull String rawExtractedText) {
        mSnapshotId = snapshotId;
        mTimestampMs = timestampMs;
        mPackageName = packageName;
        mActivityName = activityName;
        mWindowTitle = windowTitle;
        mContentHash = contentHash;
        mCompactDomJson = compactDomJson;
        mRawExtractedText = rawExtractedText;
    }

    protected AgentContextSnapshot(Parcel in) {
        mSnapshotId = in.readString();
        mTimestampMs = in.readLong();
        mPackageName = in.readString();
        mActivityName = in.readString();
        mWindowTitle = in.readString();
        mContentHash = in.readString();
        mCompactDomJson = in.readString();
        mRawExtractedText = in.readString();
    }

    public static final Creator<AgentContextSnapshot> CREATOR = new Creator<AgentContextSnapshot>() {
        @Override
        public AgentContextSnapshot createFromParcel(Parcel in) {
            return new AgentContextSnapshot(in);
        }

        @Override
        public AgentContextSnapshot[] newArray(int size) {
            return new AgentContextSnapshot[size];
        }
    };

    @NonNull
    public String getSnapshotId() { return mSnapshotId; }

    public long getTimestampMs() { return mTimestampMs; }

    @NonNull
    public String getPackageName() { return mPackageName; }

    @Nullable
    public String getActivityName() { return mActivityName; }

    @Nullable
    public String getWindowTitle() { return mWindowTitle; }

    @NonNull
    public String getContentHash() { return mContentHash; }

    @NonNull
    public String getCompactDomJson() { return mCompactDomJson; }

    @NonNull
    public String getRawExtractedText() { return mRawExtractedText; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mSnapshotId);
        dest.writeLong(mTimestampMs);
        dest.writeString(mPackageName);
        dest.writeString(mActivityName);
        dest.writeString(mWindowTitle);
        dest.writeString(mContentHash);
        dest.writeString(mCompactDomJson);
        dest.writeString(mRawExtractedText);
    }
}
