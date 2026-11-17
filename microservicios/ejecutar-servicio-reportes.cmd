@echo off
cd /d "%~dp0"
call mvnw.cmd -pl servicio-reportes spring-boot:run
pause
