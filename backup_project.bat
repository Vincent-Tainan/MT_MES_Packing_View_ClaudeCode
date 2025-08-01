@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 獲取當前日期時間 (格式: YYYYMMDD_HHMMSS)
for /f "tokens=1-6 delims=/:. " %%a in ('echo %date% %time%') do (
    set "timestamp=%%c%%a%%b_%%d%%e%%f"
)

:: 移除可能的空格
set "timestamp=%timestamp: =%"

:: 設定備份檔名
set "backup_name=MT_MPS_Packing_View_Backup_%timestamp%.7z"

echo ========================================
echo MT MES Packing View 專案備份工具
echo ========================================
echo.
echo 正在建立備份: %backup_name%
echo.

:: 檢查是否安裝 7-Zip
where 7z >nul 2>&1
if errorlevel 1 (
    echo [錯誤] 未找到 7-Zip，請先安裝 7-Zip
    echo 下載地址: https://www.7-zip.org/
    pause
    exit /b 1
)

:: 建立備份資料夾
if not exist "Backups" mkdir "Backups"

:: 建立壓縮檔，排除不必要的檔案和資料夾
echo 正在壓縮專案檔案...
7z a "Backups\%backup_name%" . ^
   -xr!*.tmp ^
   -xr!*.log ^
   -xr!build\ ^
   -xr!.gradle\ ^
   -xr!app\build\ ^
   -xr!.idea\workspace.xml ^
   -xr!.idea\tasks.xml ^
   -xr!.idea\usage.statistics.xml ^
   -xr!.idea\shelf\ ^
   -xr!Backups\ ^
   -xr!local.properties

if errorlevel 1 (
    echo.
    echo [錯誤] 備份建立失敗
    pause
    exit /b 1
)

echo.
echo ========================================
echo 備份完成！
echo 備份檔案: Backups\%backup_name%
echo ========================================

:: 顯示備份檔案大小
for %%A in ("Backups\%backup_name%") do (
    set "size=%%~zA"
    set /a "sizeMB=!size!/1048576"
    echo 檔案大小: !sizeMB! MB
)

echo.
echo 是否要開啟備份資料夾？ (Y/N)
set /p "choice="
if /i "%choice%"=="Y" (
    explorer "Backups"
)

echo.
pause