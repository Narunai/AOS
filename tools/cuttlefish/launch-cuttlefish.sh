#!/usr/bin/env bash
#
# Copyright (C) 2026 Agentic OS Project
# Script to launch Google Cuttlefish Android Virtual Device for testing AgentOS.
#

set -e

echo "==> Verifying KVM acceleration..."
if [ ! -e /dev/kvm ]; then
    echo "ERROR: /dev/kvm not found. Hardware virtualization must be enabled in BIOS/WSL2."
    exit 1
fi

echo "==> Launching Cuttlefish Virtual Device with WebRTC display..."
# Launch Cuttlefish with WebRTC control streaming on https://localhost:8443
bin/launch_cvd \
    --daemon \
    --gpu_mode=guest_swiftshader \
    --memory_mb=4096 \
    --cpus=4 \
    --start_webrtc=true

echo ""
echo "=========================================================="
echo " Cuttlefish started!"
echo " Web UI: https://localhost:8443"
echo " ADB:    adb connect 127.0.0.1:6520"
echo " To test agentd: adb shell ps -A | grep agentd"
echo " To stop: bin/stop_cvd"
echo "=========================================================="
