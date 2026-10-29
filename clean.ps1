# =====================================================
# Clean Script - Remove compiled files
# =====================================================

Write-Host "Cleaning build directory..." -ForegroundColor Cyan

if (Test-Path "build") {
    Remove-Item -Recurse -Force "build/*"
    Write-Host "Build directory cleaned!" -ForegroundColor Green
} else {
    Write-Host "Build directory doesn't exist. Nothing to clean." -ForegroundColor Gray
}
