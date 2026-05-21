@echo off
setlocal enabledelayedexpansion

:: Get the directory where the script is located
set "SCRIPT_DIR=%~dp0"
set "TARGET_EXE=%SCRIPT_DIR%FocusBean.exe"
set "SHORTCUT_PATH=%USERPROFILE%\Desktop\Focus Bean.lnk"
set "DOWNLOADS_DIR=%USERPROFILE%\Downloads"

echo =======================================================
echo     Focus Bean - Desktop Shortcut ^& Cleanup Utility
echo =======================================================
echo.

if not exist "%TARGET_EXE%" (
    echo Error: FocusBean.exe not found in this directory.
    echo Path checked: "%TARGET_EXE%"
    echo.
    echo Please run this script from the extracted Focus Bean folder.
    pause
    exit /b 1
)

echo [1/2] Updating Desktop Shortcut...
:: One-line PowerShell command to avoid line-continuation issues
powershell -NoProfile -ExecutionPolicy Bypass -Command "$wshell = New-Object -ComObject WScript.Shell; $shortcut = $wshell.CreateShortcut('%SHORTCUT_PATH%'); $shortcut.TargetPath = '%TARGET_EXE%'; $shortcut.WorkingDirectory = '%SCRIPT_DIR%'; $shortcut.Save()"

if %ERRORLEVEL% EQU 0 (
    echo       - Shortcut updated on your Desktop!
) else (
    echo       - Failed to create shortcut.
)

echo.
echo [2/2] Cleaning up older versions in Downloads...
:: One-line PowerShell command for cleanup
powershell -NoProfile -ExecutionPolicy Bypass -Command "$currentDir = [System.IO.Path]::GetFullPath('%SCRIPT_DIR%'); $downloads = '%DOWNLOADS_DIR%'; if (Test-Path $downloads) { $items = Get-ChildItem -Path $downloads -Directory -Filter 'FocusBean-*'; foreach ($item in $items) { if ($currentDir -notlike ($item.FullName + '*')) { Write-Host ('      - Removing old version: ' + $item.Name); Remove-Item -Path $item.FullName -Recurse -Force -ErrorAction SilentlyContinue; } } }"

echo.
echo =======================================================
echo Setup complete! You can now launch Focus Bean from your
echo desktop. Old version folders have been cleaned up.
echo =======================================================
pause
