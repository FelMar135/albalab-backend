@echo off
echo Iniciando microservicios de AlbaLab...
echo.

set "BASE=%~dp0"

start "usuario-service" cmd /k "cd /d %BASE%usuario-service && set MAIL_HOST=smtp.gmail.com && set MAIL_PORT=587 && set MAIL_USERNAME=marchantf7@gmail.com && set MAIL_PASSWORD=zitgkbio khpbmgjj && set MAIL_FROM=marchantf7@gmail.com && mvnw.cmd -o spring-boot:run -DskipTests"

timeout /t 8 /nobreak >nul

start "historial-service" cmd /k "cd /d %BASE%historial-service && mvnw.cmd -o spring-boot:run -DskipTests"

timeout /t 5 /nobreak >nul

start "webpay-service" cmd /k "cd /d %BASE%webpay-service && mvnw.cmd -o spring-boot:run -DskipTests"

timeout /t 5 /nobreak >nul

start "api-gateway" cmd /k "cd /d %BASE%api-gateway && mvnw.cmd -o spring-boot:run -DskipTests"

echo.
echo Todos los servicios se estan iniciando en ventanas separadas.
echo NO cierres esas ventanas mientras uses la app.
pause
