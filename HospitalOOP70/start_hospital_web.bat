@echo off
setlocal enabledelayedexpansion
title HospitalOOP70 - Government Hospital Web Server

echo =====================================================================
echo    HospitalOOP70 - Professional Government Hospital Management System  
echo    Web Application on http://localhost:8080
echo =====================================================================
echo.

:: Locate Java (Android Studio JBR or system Java)
set "JAVA_CMD=java"
if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
)
set "JAVAC_CMD=javac"
if exist "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
)

:: Test Java
"%JAVA_CMD%" -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Install Java 17+ or Android Studio.
    pause
    exit /b 1
)

:: Switch to the inner HospitalOOP70 folder (where bin/ and src/ live)
cd /d "%~dp0HospitalOOP70"

:: Classpath: bin contains all compiled classes including web server
set "CP=bin;mysql-connector-j-8.0.33.jar"

echo [1/3] Verifying compiled web server class...
if not exist "bin\hms\web\HospitalWebServer.class" (
    echo [INFO] HospitalWebServer.class missing - compiling now...
    if not exist "bin" mkdir bin
    "%JAVAC_CMD%" -d bin -cp "src;mysql-connector-j-8.0.33.jar" src\hms\web\*.java src\hms\WebLauncher.java
    if !errorlevel! neq 0 (
        echo [ERROR] Compilation failed. Check src files.
        pause
        exit /b 1
    )
)

echo [2/3] Opening browser...
start "" "http://localhost:8080"

echo [3/3] Starting web server (serving frontend\ from this directory)...
echo.
echo =====================================================================
echo   PORTAL URL:  http://localhost:8080
echo   Login:       admin / hospital123   (Administrator - full access)
echo                doctor / hospital123  (Clinical modules)
echo                receptionist / hospital123 (Intake modules)  
echo                staff / hospital123   (Ward modules)
echo.
echo   Keep this window OPEN. Press Ctrl+C to shut down the server.
echo =====================================================================
echo.

"%JAVA_CMD%" -cp "%CP%" hms.web.HospitalWebServer

echo.
echo [Web Server Stopped]
pause
