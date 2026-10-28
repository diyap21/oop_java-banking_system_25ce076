@echo off
setlocal
if not exist "bin\com\bank\Main.class" (
    echo [INFO] Binaries not found. Running build first...
    call compile.bat
    if %errorlevel% neq 0 exit /b %errorlevel%
)

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 'java' runtime was not found in your system PATH!
    echo Please make sure JDK 11 or higher is installed and added to PATH.
    pause
    exit /b 1
)

java -cp bin com.bank.Main
endlocal
