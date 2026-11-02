@echo off
setlocal
echo ========================================================
echo   Launching HospitalOOP70 in Preserved Console Mode
echo ========================================================

set JAVA_CMD=java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
)

if not exist "bin\hms\Main.class" (
    echo Bin directory not found or Main.class missing. Building first...
    call build.bat
)

"%JAVA_CMD%" -cp "bin;lib/*" hms.Main --console
endlocal
