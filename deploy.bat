@echo off
REM Script de déploiement pour Windows
REM Usage: deploy.bat [release|snapshot]

echo ======================================
echo Deploiement UEMOA QR Module
echo ======================================

REM Variables
set NEXUS_URL=http://localhost:8081
set NEXUS_USER=admin
set NEXUS_PASS=admin123
set VERSION_TYPE=%1
if "%VERSION_TYPE%"=="" set VERSION_TYPE=snapshot

REM Verifier Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven n'est pas installe ou n'est pas dans le PATH
    exit /b 1
)

REM Nettoyer et compiler
echo [INFO] Nettoyage et compilation...
call mvn clean compile
if %errorlevel% neq 0 (
    echo [ERROR] La compilation a echoue
    exit /b 1
)

REM Executer les tests
echo [INFO] Execution des tests...
call mvn test
if %errorlevel% neq 0 (
    echo [ERROR] Les tests ont echoue
    exit /b 1
)

REM Creer settings.xml temporaire
set TEMP_SETTINGS=%TEMP%\maven-settings-%RANDOM%.xml
(
echo ^<settings^>
echo     ^<servers^>
echo         ^<server^>
echo             ^<id^>nexus-releases^</id^>
echo             ^<username^>%NEXUS_USER%^</username^>
echo             ^<password^>%NEXUS_PASS%^</password^>
echo         ^</server^>
echo         ^<server^>
echo             ^<id^>nexus-snapshots^</id^>
echo             ^<username^>%NEXUS_USER%^</username^>
echo             ^<password^>%NEXUS_PASS%^</password^>
echo         ^</server^>
echo     ^</servers^>
echo ^</settings^>
) > %TEMP_SETTINGS%

REM Deployer
echo [INFO] Deploiement sur Nexus...
call mvn deploy -s %TEMP_SETTINGS%

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Deploiement reussi!
    echo Le module est disponible sur: %NEXUS_URL%
) else (
    echo [ERROR] Le deploiement a echoue
    del %TEMP_SETTINGS%
    exit /b 1
)

REM Nettoyer
del %TEMP_SETTINGS%

echo.
echo ======================================
echo Deploiement termine
echo ======================================
