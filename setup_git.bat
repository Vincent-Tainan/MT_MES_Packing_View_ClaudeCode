@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo MT MES Packing View Git 初始化工具
echo ========================================
echo.

:: 檢查是否已經是 Git 倉庫
if exist ".git" (
    echo 此專案已經是 Git 倉庫
    echo.
    goto :setup_remote
)

echo 正在初始化 Git 倉庫...
git init
if errorlevel 1 (
    echo [錯誤] Git 初始化失敗，請確認已安裝 Git
    pause
    exit /b 1
)

:: 建立 .gitignore 檔案
echo 建立 .gitignore 檔案...
(
echo # Android 編譯檔案
echo /build/
echo /app/build/
echo /local.properties
echo /.gradle/
echo
echo # IDE 檔案
echo .idea/workspace.xml
echo .idea/tasks.xml
echo .idea/usage.statistics.xml
echo .idea/dictionaries
echo .idea/shelf/
echo .idea/libraries/
echo
echo # OS 檔案
echo .DS_Store
echo Thumbs.db
echo
echo # 備份檔案
echo /Backups/
echo *.tmp
echo *.log
echo
echo # APK 檔案
echo *.apk
echo *.aab
) > .gitignore

echo 添加專案檔案到 Git...
git add .
git commit -m "Initial commit: MT MES Packing View Android App"

:setup_remote
echo.
echo ========================================
echo Git 遠端倉庫設定
echo ========================================
echo.
echo 請選擇遠端倉庫選項:
echo 1. GitHub
echo 2. GitLab
echo 3. 自定義 Git 服務器
echo 4. 跳過遠端設定
echo.
set /p "choice=請輸入選項 (1-4): "

if "%choice%"=="1" goto :github_setup
if "%choice%"=="2" goto :gitlab_setup
if "%choice%"=="3" goto :custom_setup
if "%choice%"=="4" goto :skip_remote
goto :setup_remote

:github_setup
echo.
echo GitHub 設定:
echo 1. 請先在 GitHub 建立新的私有倉庫
echo 2. 倉庫名稱建議: MT_MPS_Packing_View
echo 3. 取得倉庫 URL (格式: https://github.com/yourusername/MT_MPS_Packing_View.git)
echo.
set /p "repo_url=請輸入 GitHub 倉庫 URL: "
goto :add_remote

:gitlab_setup
echo.
echo GitLab 設定:
echo 1. 請先在 GitLab 建立新的私有專案
echo 2. 專案名稱建議: MT_MPS_Packing_View
echo 3. 取得專案 URL (格式: https://gitlab.com/yourusername/MT_MPS_Packing_View.git)
echo.
set /p "repo_url=請輸入 GitLab 專案 URL: "
goto :add_remote

:custom_setup
echo.
echo 自定義 Git 服務器設定:
echo 請輸入您的 Git 倉庫 URL
echo.
set /p "repo_url=請輸入倉庫 URL: "
goto :add_remote

:add_remote
echo.
echo 正在添加遠端倉庫...
git remote add origin "%repo_url%"
if errorlevel 1 (
    echo [錯誤] 添加遠端倉庫失敗
    goto :setup_remote
)

echo 正在推送到遠端倉庫...
git branch -M main
git push -u origin main
if errorlevel 1 (
    echo.
    echo [警告] 推送失敗，可能需要身份驗證
    echo 請手動執行: git push -u origin main
    echo.
)

echo.
echo ========================================
echo Git 設定完成！
echo 遠端倉庫: %repo_url%
echo ========================================
goto :end

:skip_remote
echo.
echo 跳過遠端倉庫設定
echo 您可以稍後使用以下命令添加遠端倉庫:
echo git remote add origin [倉庫URL]
echo git push -u origin main

:end
echo.
echo Git 初始化完成！
echo.
pause