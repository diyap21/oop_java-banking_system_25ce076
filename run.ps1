if (-not (Test-Path "bin\com\bank\Main.class")) {
    Write-Host "[INFO] Binaries not found. Triggering compilation first..." -ForegroundColor Yellow
    & "$PSScriptRoot\compile.ps1"
}

java -cp "$PSScriptRoot\bin" com.bank.Main
