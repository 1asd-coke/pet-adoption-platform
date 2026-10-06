@echo off
title ClawPet Launcher
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start.ps1"
echo.
echo ============================================
echo  If you see this, the script crashed above.
echo  Check start.log for details.
echo ============================================
pause
