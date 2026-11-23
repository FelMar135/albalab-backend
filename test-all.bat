@echo off
setlocal

call :test usuario-service
if errorlevel 1 exit /b 1

call :test admin-service
if errorlevel 1 exit /b 1

call :test colaborador-service
if errorlevel 1 exit /b 1

call :test product-service
if errorlevel 1 exit /b 1

call :test webpay-service
if errorlevel 1 exit /b 1

call :test api-gateway
if errorlevel 1 exit /b 1

echo.
echo Todas las pruebas pasaron correctamente.
exit /b 0

:test
echo.
echo ========================================
echo Ejecutando pruebas de %~1
echo ========================================
pushd "%~1"
call .\mvnw.cmd test -s ..\configuracion-maven\settings.xml
set RESULT=%ERRORLEVEL%
popd
exit /b %RESULT%
