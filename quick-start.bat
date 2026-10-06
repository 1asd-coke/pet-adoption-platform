@echo off
title ClawPet Quick Start
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\quick-start.ps1" %*
echo.
echo Script finished. If it crashed, the error is shown above.
pause
