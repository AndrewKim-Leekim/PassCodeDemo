
@echo off
setlocal

cd /d "%~dp0"

set "JAVA_CMD=java"
if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)

set "FX_LIB_DIR=target\javafx-libs"
if not exist "%FX_LIB_DIR%" (
    echo JavaFX runtime libraries were not found in %FX_LIB_DIR%.
    echo Run "mvn package" first so the JavaFX dependencies are copied locally.
    exit /b 1
)

"%JAVA_CMD%" --module-path "%FX_LIB_DIR%" --add-modules=javafx.controls,javafx.media,javafx.swing -cp target\PassCodeDemo-1.0-SNAPSHOT.jar com.mycompany.passcodedemo.PassCodeDemo

endlocal
pause
