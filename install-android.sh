#!/bin/bash

# ==================================================
# EcoGo Android 安装脚本 (Linux/macOS)
# ==================================================
# 功能: 编译并安装 Android Debug APK
# ==================================================

set -e

echo ""
echo "=========================================="
echo "  EcoGo Android 安装脚本"
echo "=========================================="
echo ""

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$SCRIPT_DIR/android-app"

# 检查目录
if [ ! -d "$ANDROID_DIR" ]; then
    echo "❌ 错误: android-app 目录不存在"
    exit 1
fi

cd "$ANDROID_DIR"

# 检查 gradlew
if [ ! -f "gradlew" ]; then
    echo "❌ 错误: gradlew 文件不存在"
    exit 1
fi

# 检查设备连接
echo "[1/4] 检查 Android 设备..."
if ! command -v adb &> /dev/null; then
    echo "❌ 错误: adb 未安装"
    echo "请安装 Android SDK Platform Tools"
    exit 1
fi

device_count=$(adb devices | grep -v "List" | grep device | wc -l)
if [ "$device_count" -eq 0 ]; then
    echo "⚠️  警告: 没有连接的设备或模拟器"
    echo "请连接设备后重试"
    exit 1
fi

echo "✓ 已连接 $device_count 个设备"

# 清理项目
echo ""
echo "[2/4] 清理项目..."
./gradlew clean

# 编译项目
echo ""
echo "[3/4] 编译 Debug APK..."
./gradlew assembleDebug

# 安装 APK
echo ""
echo "[4/4] 安装 APK 到设备..."
./gradlew installDebug

echo ""
echo "=========================================="
echo "✓ 安装完成！"
echo "=========================================="
echo ""
echo "启动应用:"
echo "  adb shell am start -n com.ecogo/.MainActivity"
echo ""

# 询问是否启动
read -p "是否立即启动应用? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    adb shell am start -n com.ecogo/.MainActivity
    echo "✓ 应用已启动"
fi
