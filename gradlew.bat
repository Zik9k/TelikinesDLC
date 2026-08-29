@echo off
setlocal
set "GRADLE_VERSION=9.2.0"
set "GRADLE_HOME=%~dp0.gradle\gradle-%GRADLE_VERSION%"
if exist "%GRADLE_HOME%\bin\gradle.bat" goto RUN
set "ZIP=%~dp0.gradle\gradle-%GRADLE_VERSION%-bin.zip"
if not exist "%~dp0.gradle" mkdir "%~dp0.gradle"
echo Downloading Gradle %GRADLE_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%~dp0.gradle'"
if errorlevel 1 exit /b 1
:RUN
call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
