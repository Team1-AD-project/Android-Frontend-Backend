@echo off
echo ========================================
echo   Checking for compilation errors...
echo ========================================
echo.

call gradlew.bat build --stacktrace > build-log.txt 2>&1

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   NO ERRORS - Build Successful!
    echo ========================================
    echo.
    type build-log.txt | findstr /C:"BUILD SUCCESSFUL"
    echo.
    echo Full log saved to: build-log.txt
) else (
    echo.
    echo ========================================
    echo   ERRORS FOUND!
    echo ========================================
    echo.
    echo Showing errors:
    type build-log.txt | findstr /C:"error:" /C:"Error" /C:"FAILURE"
    echo.
    echo Full log saved to: build-log.txt
    echo.
    echo Common fixes:
    echo   1. Missing imports - Check import statements
    echo   2. Syntax errors - Check for typos
    echo   3. Unresolved references - Make sure all files are created
    echo.
)

pause
