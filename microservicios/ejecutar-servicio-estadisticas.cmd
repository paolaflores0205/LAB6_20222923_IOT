@echo off
cd /d "%~dp0"
call mvnw.cmd -pl servicio-estadisticas spring-boot:run
pause
