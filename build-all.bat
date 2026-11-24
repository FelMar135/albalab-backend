@echo off
setlocal

call :build usuario-service
if errorlevel 1 exit /b 1

call :build admin-service
if errorlevel 1 exit /b 1

call :build colaborador-service
if errorlevel 1 exit /b 1

call :build product-service
if errorlevel 1 exit /b 1

call :build webpay-service
if errorlevel 1 exit /b 1

call :build historial-service
if errorlevel 1 exit /b 1

call :build api-gateway
if errorlevel 1 exit /b 1

echo.
echo Todos los microservicios fueron compilados correctamente.
exit /b 0

:build
echo.
echo ========================================
echo Compilando %~1
echo ========================================
pushd "%~1"
call .\mvnw.cmd clean package -DskipTests -s ..\configuracion-maven\settings.xml
set RESULT=%ERRORLEVEL%
popd
exit /b %RESULT%
