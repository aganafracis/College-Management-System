# =====================================================
# Compile Script for Team3 College Management System
# Compiles ALL .java files in the project directory
# =====================================================

Write-Host "Compiling Java source files..." -ForegroundColor Cyan

# Create build directory if it doesn't exist
if (-not (Test-Path "build")) {
    New-Item -ItemType Directory -Path "build" | Out-Null
}

# Find ALL .java files recursively in the ENTIRE project directory
# Excludes build directory to avoid compiling already compiled files
$javaFiles = Get-ChildItem -Path "." -Filter "*.java" -Recurse | 
             Where-Object { $_.FullName -notlike "*\build\*" } | 
             Select-Object -ExpandProperty FullName

if ($javaFiles.Count -eq 0) {
    Write-Host "No Java files found in project directory!" -ForegroundColor Red
    exit 1
}

Write-Host "Found $($javaFiles.Count) Java files to compile..." -ForegroundColor Gray

# Compile all Java files (new, old, edited, non-edited - everything!)
javac -cp ".;lib/*" -d build $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    Write-Host "Compiled files are in the 'build' directory" -ForegroundColor Gray
    Write-Host "Total files compiled: $($javaFiles.Count)" -ForegroundColor Gray
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}
