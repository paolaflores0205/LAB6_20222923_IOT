@echo off
cd /d "%~dp0"
call mvnw.cmd -pl eureka-server spring-boot:run
pause
