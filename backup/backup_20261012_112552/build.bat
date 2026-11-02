@echo off
setlocal
echo ========================================================
echo   Compiling Government Hospital Management System (HMS)
echo ========================================================

set JAVAC_CMD=javac
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    set "JAVAC_CMD=C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
)

if not exist "bin" mkdir bin

echo Using compiler: %JAVAC_CMD%
dir /s /b src\*.java > sources.txt
"%JAVAC_CMD%" -cp "lib/*" -d bin -encoding UTF-8 @sources.txt
set COMPILE_STATUS=%ERRORLEVEL%
del sources.txt

if %COMPILE_STATUS% EQU 0 (
    echo [SUCCESS] Compilation completed without errors.
) else (
    echo [ERROR] Compilation failed.
)
endlocal
