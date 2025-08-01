@echo off
chcp 65001 >nul

echo ========================================
echo MT MES Packing View 版本管理工具
echo ========================================
echo.

echo 請選擇版本類型:
echo 1. 主要版本 (Major) - 重大功能更新
echo 2. 次要版本 (Minor) - 新功能添加
echo 3. 修補版本 (Patch) - 錯誤修正
echo 4. 查看當前版本
echo 5. 退出
echo.
set /p "choice=請輸入選項 (1-5): "

if "%choice%"=="1" goto :major
if "%choice%"=="2" goto :minor
if "%choice%"=="3" goto :patch
if "%choice%"=="4" goto :current
if "%choice%"=="5" goto :end
goto :start

:current
echo.
echo 當前版本信息:
git describe --tags --abbrev=0 2>nul || echo 尚未創建版本標籤
echo.
git log --oneline -5
echo.
pause
goto :start

:major
set "version_type=major"
set /p "version=請輸入主要版本號 (例如: 2.0.0): "
goto :create_tag

:minor
set "version_type=minor"
set /p "version=請輸入次要版本號 (例如: 1.1.0): "
goto :create_tag

:patch
set "version_type=patch"
set /p "version=請輸入修補版本號 (例如: 1.0.1): "
goto :create_tag

:create_tag
echo.
set /p "message=請輸入版本更新說明: "

echo.
echo 正在創建版本標籤 v%version%...

git add .
git commit -m "Release v%version%: %message%

🤖 Generated with Claude Code
Co-Authored-By: Claude <noreply@anthropic.com>"

git tag -a "v%version%" -m "MT MES Packing View v%version%

%message%

版本類型: %version_type%
創建時間: %date% %time%

🤖 Generated with Claude Code"

echo 推送到 GitHub...
git push
git push origin "v%version%"

echo.
echo ========================================
echo 版本 v%version% 創建成功！
echo GitHub: https://github.com/Vincent-Tainan/MT_MES_Packing_View_ClaudeCode/releases
echo ========================================
echo.
pause
goto :start

:start
cls
goto :begin

:begin
echo ========================================
echo MT MES Packing View 版本管理工具
echo ========================================
echo.

echo 請選擇版本類型:
echo 1. 主要版本 (Major) - 重大功能更新
echo 2. 次要版本 (Minor) - 新功能添加
echo 3. 修補版本 (Patch) - 錯誤修正
echo 4. 查看當前版本
echo 5. 退出
echo.
set /p "choice=請輸入選項 (1-5): "

if "%choice%"=="1" goto :major
if "%choice%"=="2" goto :minor
if "%choice%"=="3" goto :patch
if "%choice%"=="4" goto :current
if "%choice%"=="5" goto :end
goto :begin

:end
echo.
echo 感謝使用版本管理工具！
pause