@echo off
setlocal
echo ==================================================================
echo   Building Apex Secure Banking System (25CE076)
echo ==================================================================
if not exist "bin" mkdir "bin"

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 'javac' compiler was not found in your system PATH!
    echo Please make sure JDK 11 or higher is installed and added to PATH.
    pause
    exit /b 1
)

echo [1/2] Discovering source files...
dir /s /b src\*.java > sources.txt

echo [2/2] Compiling Java classes into bin\...
javac -d bin -encoding UTF-8 @sources.txt
set BUILD_STATUS=%errorlevel%
del sources.txt

if %BUILD_STATUS% equ 0 (
    echo [SUCCESS] Compilation finished successfully!
    echo You can now start the application using: run.bat
) else (
    echo [ERROR] Compilation failed with error code %BUILD_STATUS%.
)
endlocal
