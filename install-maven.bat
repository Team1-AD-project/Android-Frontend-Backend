@echo off
chcp 65001 >nul
echo ========================================
echo Maven 自动安装脚本
echo ========================================
echo.

REM 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ❌ 需要管理员权限！
    echo.
    echo 请执行以下步骤：
    echo 1. 右键点击此文件 (install-maven.bat^)
    echo 2. 选择 "以管理员身份运行"
    echo.
    pause
    exit /b 1
)

echo ✅ 管理员权限检查通过
echo.

REM 检查是否已安装 Maven
where mvn >nul 2>&1
if %errorLevel% equ 0 (
    echo ✅ Maven 已经安装！
    mvn -version
    echo.
    pause
    exit /b 0
)

echo 📦 开始安装 Maven...
echo.

REM 安装 Chocolatey（如果未安装）
where choco >nul 2>&1
if %errorLevel% neq 0 (
    echo 正在安装 Chocolatey 包管理器...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
    
    if %errorLevel% neq 0 (
        echo ❌ Chocolatey 安装失败
        echo 请尝试手动安装：https://docs.chocolatey.org/en-us/choco/setup
        pause
        exit /b 1
    )
    
    echo ✅ Chocolatey 安装成功
    echo.
    
    REM 刷新环境变量
    call refreshenv
)

echo 正在使用 Chocolatey 安装 Maven...
choco install maven -y

if %errorLevel% neq 0 (
    echo ❌ Maven 安装失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ Maven 安装成功！
echo ========================================
echo.

REM 刷新环境变量
call refreshenv

REM 验证安装
echo 验证 Maven 安装：
mvn -version

echo.
echo 📝 后续步骤：
echo 1. 关闭所有 PowerShell/CMD 窗口
echo 2. 重新打开一个新的终端
echo 3. 运行: cd C:\Users\csls\Desktop\ad-ui
echo 4. 运行: mvn spring-boot:run
echo.
pause
