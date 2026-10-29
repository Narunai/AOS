#!/usr/bin/env bash
#
# Copyright (C) 2026 Agentic OS Project
# Script to apply AgentOS Phase 2 patches onto an AOSP / LineageOS 22 source tree.
#

set -e

if [ -z "$1" ]; then
    echo "Usage: ./apply-aosp-patches.sh <path-to-aosp-root>"
    exit 1
fi

AOSP_ROOT="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "==> Target AOSP Root: $AOSP_ROOT"
echo "==> AgentOS Patches: $REPO_ROOT/aosp-patches"

# 1. Copy AIDL & Core Framework files
echo "[1/5] Installing Agent Context Bus AIDL and Manager into frameworks/base..."
mkdir -p "$AOSP_ROOT/frameworks/base/core/java/android/app/agent"
cp -r "$REPO_ROOT/aosp-patches/frameworks_base/core/java/android/app/agent/"* \
      "$AOSP_ROOT/frameworks/base/core/java/android/app/agent/"

# 2. Copy SystemServer Service
echo "[2/5] Installing AgentContextManagerService into frameworks/base/services..."
mkdir -p "$AOSP_ROOT/frameworks/base/services/core/java/com/android/server/agent"
cp -r "$REPO_ROOT/aosp-patches/frameworks_base/services/core/java/com/android/server/agent/"* \
      "$AOSP_ROOT/frameworks/base/services/core/java/com/android/server/agent/"

# 3. Apply Git Patches for WMS, ATMS, and SystemServer
echo "[3/5] Applying Framework git patches..."
cd "$AOSP_ROOT/frameworks/base"
git apply "$REPO_ROOT/aosp-patches/frameworks_base/0001-Add-AgentContextManagerService-to-SystemServer.patch" || echo "Note: 0001 patch might already be applied."
git apply "$REPO_ROOT/aosp-patches/frameworks_base/0002-Hook-ActivityTaskManagerService-lifecycle.patch" || echo "Note: 0002 patch might already be applied."
git apply "$REPO_ROOT/aosp-patches/frameworks_base/0003-Hook-WindowManagerService-for-direct-DOM-extraction.patch" || echo "Note: 0003 patch might already be applied."

# 4. Copy Native C++ Daemon (agentd)
echo "[4/5] Installing Native Daemon (agentd) into system/agentd..."
mkdir -p "$AOSP_ROOT/system/agentd"
cp -r "$REPO_ROOT/aosp-patches/system_agentd/"* "$AOSP_ROOT/system/agentd/"

# 5. Copy SELinux policies
echo "[5/5] Installing SELinux policies into system/sepolicy/private..."
mkdir -p "$AOSP_ROOT/system/sepolicy/private"
cp "$REPO_ROOT/aosp-patches/sepolicy/private/agentd.te" "$AOSP_ROOT/system/sepolicy/private/"
cat "$REPO_ROOT/aosp-patches/sepolicy/private/service.te" >> "$AOSP_ROOT/system/sepolicy/private/service.te"
cat "$REPO_ROOT/aosp-patches/sepolicy/private/service_contexts" >> "$AOSP_ROOT/system/sepolicy/private/service_contexts"
cat "$REPO_ROOT/aosp-patches/sepolicy/private/system_server.te" >> "$AOSP_ROOT/system/sepolicy/private/system_server.te"

echo ""
echo "=========================================================="
echo " AgentOS Phase 2 patches applied successfully!"
echo " Next steps:"
echo "   source build/envsetup.sh"
echo "   lunch lineage_arm64-userdebug (or aosp_cf_x86_64_phone-userdebug)"
echo "   m -j\$(nproc) agentd framework-minus-apex services"
echo "=========================================================="
