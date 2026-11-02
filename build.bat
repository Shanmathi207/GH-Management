@echo off
setlocal
echo ========================================================
echo   Compiling Government Hospital Management System (HMS)
echo ========================================================

rem Prioritize compatible Java 21 JDK installation
if exist "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
    set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
) else if defined JAVA_HOME (
    set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVAC_CMD=javac"
    set "JAVA_CMD=java"
)

if not exist "bin" mkdir bin

echo Using compiler: "%JAVAC_CMD%"
rem Generate sources.txt list of all Java files
del sources.txt 2>nul
for /R src %%f in (*.java) do echo %%f>>sources.txt
"%JAVAC_CMD%" -cp "lib/*" -d bin -encoding UTF-8 @sources.txt
set COMPILE_STATUS=%ERRORLEVEL%
del sources.txt

if %COMPILE_STATUS% EQU 0 (
    echo [SUCCESS] Compilation completed without errors.
) else (
    echo [ERROR] Compilation failed with code %COMPILE_STATUS%.
)

exit /b %COMPILE_STATUS%
endlocal
