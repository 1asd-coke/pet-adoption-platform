@echo off
REM ============================================================
REM  Claw Pet one-click deploy (Docker) - Windows launcher
REM
REM  This is a thin wrapper around scripts/deploy.ps1.
REM  It exists because double-clicking a .ps1 does not always work
REM  (Windows blocks unsigned scripts by default).
REM
REM  Usage:  deploy.bat            build + start
REM          deploy.bat down       stop (keep data)
REM          deploy.bat ps         show status
REM          deploy.bat logs backend
REM ============================================================
title ClawPet Deploy

where docker >nul 2>nul
if errorlevel 1 goto nodocker

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\deploy.ps1" %*
goto end

:nodocker
echo.
echo ============================================================
echo   Docker was not found on this machine.
echo ============================================================
echo.
echo   This deploy method needs Docker Desktop:
echo       https://www.docker.com/products/docker-desktop/
echo.
echo   No Docker? You can still run the project locally instead:
echo       double-click start.bat
echo   (that one uses your own JDK / Maven / Node / MySQL / Redis)
echo.
echo ============================================================

:end
echo.
echo Script finished. If it crashed, the error is shown above.
pause
