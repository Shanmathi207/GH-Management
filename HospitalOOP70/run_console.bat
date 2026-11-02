@echo off
setlocal
echo ========================================================
echo   Launching HospitalOOP70 in Preserved Console Mode
echo ========================================================

rem Prioritize compatible Java 21 JDK installation
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
) else if defined JAVA_HOME (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

if not exist "bin\hms\Main.class" (
    echo [INFO] Bin directory not found or Main.class missing. Building first...
    call build.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed. Cannot launch console application.
        exit /b 1
    )
)

echo Using runtime: "%JAVA_CMD%"
echo Starting Console Application...
"%JAVA_CMD%" -cp "bin;lib/*" hms.Main --console
endlocal
