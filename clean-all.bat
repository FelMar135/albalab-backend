@echo off
setlocal

call :clean usuario-service
call :clean admin-service
call :clean colaborador-service
call :clean product-service
call :clean webpay-service
call :clean api-gateway

echo.
echo Limpieza terminada.
exit /b 0

:clean
pushd "%~1"
call .\mvnw.cmd clean -s ..\configuracion-maven\settings.xml
popd
exit /b 0
