@REM Maven Wrapper startup batch script
@REM -----------------------------------
@echo off
@setlocal

set WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%~dp0\.mvn\wrapper\maven-wrapper.properties"
set MAVEN_PROJECTBASEDIR=%~dp0

@REM Find java.exe
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo Java not found in PATH
goto error

:init
@REM Collect all command line arguments
set MAVEN_CMD_LINE_ARGS=
:collectArgs
if "%~1"=="" goto doneCollecting
set MAVEN_CMD_LINE_ARGS=%MAVEN_CMD_LINE_ARGS% %1
shift
goto collectArgs
:doneCollecting

@REM Download Maven if needed
for /f "usebackq tokens=1,2 delims==" %%a in (%WRAPPER_PROPERTIES%) do (
    if "%%a"=="distributionUrl" set DOWNLOAD_URL=%%b
)

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto execute

echo Downloading Maven...
mkdir "%MAVEN_HOME%" 2>NUL
powershell -Command "& {Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%TEMP%\maven.zip'; Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force; Remove-Item '%TEMP%\maven.zip'}"

:execute
"%MAVEN_HOME%\bin\mvn.cmd" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
