Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   Building Apex Secure Banking System (25CE076) [PowerShell]    " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$javaSources = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
if ($javaSources.Count -eq 0) {
    Write-Host "[ERROR] No Java source files discovered in src/ directory!" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Found $($javaSources.Count) source files. Compiling..." -ForegroundColor Yellow
javac -d bin -encoding UTF-8 $javaSources

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation successful! Classes compiled to bin/" -ForegroundColor Green
    Write-Host "Execute with: .\run.ps1" -ForegroundColor Cyan
} else {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
}
