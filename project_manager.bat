@echo off
chcp 65001 >nul

echo ========================================
echo MT MES Packing View 專案管理工具
echo ========================================
echo.
echo 請選擇操作:
echo 1. 專案備份 (壓縮打包)
echo 2. Git 推送備份
echo 3. 初始化 Git 倉庫
echo 4. 查看備份清單
echo 5. 退出
echo.
set /p "choice=請輸入選項 (1-5): "

if "%choice%"=="1" goto :backup
if "%choice%"=="2" goto :git_backup
if "%choice%"=="3" goto :git_init
if "%choice%"=="4" goto :list_backups
if "%choice%"=="5" goto :end
goto :main

:backup
echo.
echo 正在執行專案備份...
call backup_project.bat
goto :main

:git_backup
echo.
echo 正在執行 Git 備份...
if not exist ".git" (
    echo [錯誤] 此專案尚未初始化 Git 倉庫
    echo 請先選擇選項 3 初始化 Git
    echo.
    pause
    goto :main
)

:: 檢查是否有變更
git status --porcelain >nul 2>&1
if errorlevel 1 (
    echo [錯誤] Git 狀態檢查失敗
    pause
    goto :main
)

:: 自動提交所有變更
echo 正在添加變更檔案...
git add .

echo 輸入提交訊息 (留空使用預設訊息):
set /p "commit_msg="
if "%commit_msg%"=="" (
    for /f "tokens=1-6 delims=/:. " %%a in ('echo %date% %time%') do (
        set "timestamp=%%c-%%a-%%b %%d:%%e"
    )
    set "commit_msg=Backup: !timestamp!"
)

git commit -m "%commit_msg%"
if errorlevel 1 (
    echo 沒有需要提交的變更
) else (
    echo 正在推送到遠端倉庫...
    git push
    if errorlevel 1 (
        echo [警告] 推送失敗，請檢查網路連線或身份驗證
    ) else (
        echo Git 備份完成！
    )
)
echo.
pause
goto :main

:git_init
echo.
echo 正在初始化 Git 倉庫...
call setup_git.bat
goto :main

:list_backups
echo.
echo ========================================
echo 備份檔案清單
echo ========================================
if exist "Backups" (
    dir "Backups\*.7z" /b /o-d 2>nul
    if errorlevel 1 (
        echo 沒有找到備份檔案
    )
) else (
    echo 備份資料夾不存在
)
echo.
pause
goto :main

:main
cls
goto :start

:start
echo ========================================
echo MT MES Packing View 專案管理工具
echo ========================================
echo.
echo 請選擇操作:
echo 1. 專案備份 (壓縮打包)
echo 2. Git 推送備份
echo 3. 初始化 Git 倉庫
echo 4. 查看備份清單
echo 5. 退出
echo.
set /p "choice=請輸入選項 (1-5): "

if "%choice%"=="1" goto :backup
if "%choice%"=="2" goto :git_backup
if "%choice%"=="3" goto :git_init
if "%choice%"=="4" goto :list_backups
if "%choice%"=="5" goto :end
goto :start

:end
echo.
echo 感謝使用！
pause