@echo off
setlocal
cd /d "%~dp0"
if exist "build\Release\IllusionOfTimeSNESRecomp.exe" (
    start "" "build\Release\IllusionOfTimeSNESRecomp.exe" --launcher
) else if exist "IllusionOfTimeSNESRecomp.exe" (
    start "" "IllusionOfTimeSNESRecomp.exe" --launcher
) else (
    echo No se encontro IllusionOfTimeSNESRecomp.exe. Ejecuta primero COMPILAR.bat.
    pause
)
exit /b 0
