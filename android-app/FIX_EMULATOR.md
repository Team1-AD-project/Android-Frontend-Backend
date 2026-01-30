# 🔧 修复模拟器终止问题

## 问题
```
Error running 'app'
The emulator process for AVD Pixel_8_Pro has terminated.
```

## 🚀 解决方案（按顺序尝试）

### 方案 1: 冷启动 + 擦除数据（最常见有效）

1. **打开 Device Manager**
   - Android Studio > Tools > Device Manager
   - 或点击右侧边栏的设备图标 📱

2. **找到 Pixel_8_Pro 设备**
   - 在设备列表中找到它

3. **点击设备右侧的 ⋮ (三个点)**
   - 选择 "Cold Boot Now"（冷启动）
   - 如果还不行，再次点击 ⋮
   - 选择 "Wipe Data"（擦除数据）然后重启

### 方案 2: 创建新的模拟器（推荐配置）

1. **Device Manager > Create Device**

2. **选择设备**
   ```
   ✅ 推荐: Pixel 5 或 Pixel 6 (比 Pixel 8 Pro 更稳定)
   点击 Next
   ```

3. **选择系统镜像**
   ```
   ✅ API Level 34 (Android 14.0)
   ✅ Target: Android 14.0 (Google APIs)
   ✅ ABI: x86_64
   
   如果未下载，点击镜像旁的 Download 按钮
   点击 Next
   ```

4. **配置 AVD**
   ```
   AVD Name: EcoGo_Test
   
   ⚙️ 高级设置 (Show Advanced Settings):
   - RAM: 2048 MB (如果电脑内存 > 8GB，可用 4096)
   - VM heap: 256 MB
   - Internal Storage: 2048 MB
   - Graphics: Hardware - GLES 2.0 (如果失败，改用 Software)
   - Boot option: Cold boot（冷启动）
   ```

5. **点击 Finish**

### 方案 3: 修复硬件加速（Windows 常见问题）

#### 检查 Hyper-V 状态

**选项 A: 如果需要其他虚拟化软件（VMware 等）**
```
禁用 Hyper-V:
1. 以管理员身份打开 PowerShell
2. 运行: bcdedit /set hypervisorlaunchtype off
3. 重启电脑
```

**选项 B: 如果只用 Android 模拟器**
```
启用 Hyper-V:
1. 控制面板 > 程序 > 启用或关闭 Windows 功能
2. 勾选:
   ✅ Hyper-V
   ✅ Windows Hypervisor Platform
   ✅ Virtual Machine Platform
3. 重启电脑
```

#### 安装 Intel HAXM（如果使用 Intel CPU）

1. 下载: https://github.com/intel/haxm/releases
2. 安装 `haxm-windows_v7_8_0.zip`
3. 重启电脑

### 方案 4: 调整图形设置

1. **Device Manager > 找到模拟器 > ⋮ > Edit**
2. **Show Advanced Settings**
3. **Graphics 改为: Software - GLES 2.0**
4. **保存并重启模拟器**

### 方案 5: 增加模拟器资源

1. **编辑模拟器配置**
   - Device Manager > 模拟器 > ⋮ > Edit

2. **调整设置**
   ```
   RAM: 2048 → 1024 (降低内存)
   Graphics: Hardware → Software (降低图形要求)
   Boot: Quick Boot → Cold Boot (改为冷启动)
   ```

## 🔍 查看详细错误日志

如果以上方法都不行，查看详细日志：

### 方法 1: Android Studio Event Log
```
View > Tool Windows > Event Log
查看最近的错误信息
```

### 方法 2: 模拟器日志
```
位置: C:\Users\csls\.android\avd\Pixel_8_Pro.avd\emulator.log
用记事本打开，查看最后几行
```

### 方法 3: 从命令行启动（查看实时错误）
```powershell
# 找到 emulator 路径（通常在 Android SDK 中）
# 例如: C:\Users\csls\AppData\Local\Android\Sdk\emulator

cd "C:\Users\csls\AppData\Local\Android\Sdk\emulator"
.\emulator.exe -avd Pixel_8_Pro -verbose

# 这会显示详细的错误信息
```

## 🎯 推荐的稳定配置

如果重新创建模拟器，使用这个配置最稳定：

```
设备: Pixel 5
API Level: 34 (Android 14)
ABI: x86_64
RAM: 2048 MB
Internal Storage: 2048 MB
Graphics: Hardware - GLES 2.0
Boot: Cold boot
```

## 📝 常见错误及原因

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| Emulator terminated | AVD 损坏 | 擦除数据或重建 AVD |
| Cannot launch emulator | 硬件加速问题 | 安装 HAXM 或配置 Hyper-V |
| HAX is not working | Intel HAXM 未安装 | 安装 Intel HAXM |
| Insufficient memory | 内存不足 | 降低 RAM 设置 |
| Graphics initialization failed | 图形驱动问题 | 改用 Software 渲染 |

## ✅ 验证修复

模拟器成功启动的标志：
```
✅ 看到 Android 启动动画
✅ 进入 Android 主屏幕（可能需要 1-3 分钟）
✅ Device Manager 显示设备为 "Online"
✅ Android Studio 可以在设备下拉菜单中看到模拟器
```

## 🚀 下一步

模拟器成功启动后：
```
1. 在 Android Studio 顶部选择该模拟器
2. 点击绿色 Run 按钮 ▶️
3. 等待应用安装和启动
```

---

**提示**: 如果问题持续，请：
1. 查看 Event Log 中的具体错误
2. 将完整错误信息发给我
3. 我会帮您详细诊断
