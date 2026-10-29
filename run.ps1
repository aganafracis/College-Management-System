# =====================================================
# Run Script for Team3 College Management System
# =====================================================

Write-Host "Starting College Management System..." -ForegroundColor Cyan

# Check if compiled files exist
if (-not (Test-Path "build/LoginFrame.class")) {
    Write-Host "Compiled files not found. Running compile script first..." -ForegroundColor Yellow
    & .\compile.ps1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation failed. Cannot run application." -ForegroundColor Red
        exit 1
    }
}

# Run the application
Write-Host "Launching Login Screen..." -ForegroundColor Green
java -cp "build;lib/mysql-connector-j-8.3.0.jar" LoginFrame
