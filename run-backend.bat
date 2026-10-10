@echo off
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is not installed or not available on PATH.
    echo Install Maven and then run this script again.
    pause
    exit /b 1
)

call mvn spring-boot:run
if errorlevel 1 (
    echo.
    echo Backend failed to start.
    pause
)
