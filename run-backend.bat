@echo off
chcp 65001 >nul
echo 正在启动 EcoGo 后端...
cd /d "%~dp0"

set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
if not exist "%PS%" (
    echo 未找到 PowerShell。请用 IntelliJ 打开本项目，运行 EcoGoApplication.java。
    echo 详见 START_BACKEND.md
    pause
    exit /b 1
)

echo 使用 Maven Wrapper 启动，首次运行可能需下载依赖…
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "Set-Location '%~dp0'; $env:Path = \"$env:SystemRoot\System32\WindowsPowerShell\v1.0;\" + $env:Path; .\mvnw.cmd spring-boot:run"
if errorlevel 1 (
    echo.
    echo 启动失败。建议：用 IntelliJ 打开项目，运行 EcoGoApplication.java。
    echo 详见 START_BACKEND.md
    pause
)
