@echo off
echo ========================================
echo   EcoGo Android Build Script
echo ========================================
echo.

echo [1/3] Cleaning project...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)
echo Clean successful!
echo.

echo [2/3] Building project...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    echo.
    echo Check the error messages above.
    echo Common solutions:
    echo - Make sure you're in the android-app directory
    echo - Run: gradlew clean
    echo - Check your JDK version (should be JDK 17)
    pause
    exit /b 1
)
echo Build successful!
echo.

echo [3/3] Checking output...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ========================================
    echo   BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Next steps:
    echo   1. Connect your Android device or start emulator
    echo   2. Run: gradlew installDebug
    echo   3. Or open in Android Studio and click Run
    echo.
) else (
    echo WARNING: APK not found in expected location
)

pause
