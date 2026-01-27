@echo off
TITLE SPS-F1 Strategy System
CLS

ECHO ==========================================
ECHO      AVVIO SISTEMA SPS-F1 STRATEGY
ECHO ==========================================
ECHO.

:: --- 1. PULIZIA PORTE (Uccide processi zombie) ---
ECHO [1/4] Pulizia sistema in corso...
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM ml-service.exe >nul 2>&1
timeout /t 1 >nul

:: --- 2. AVVIO PYTHON ---
ECHO [2/4] Avvio Motore IA...
cd ml-python
:: Lancia Python in background (start /B) dalla cartella dist
start /B "" dist\ml-service.exe >nul 2>&1
cd ..

:: --- 3. BARRA DI CARICAMENTO ---
:: Controlla se la porta 5000 risponde
:WAIT_LOOP
curl -s http://127.0.0.1:5000 >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    <nul set /p =▓
    timeout /t 1 >nul
    GOTO WAIT_LOOP
)
ECHO.
ECHO [OK] Modello Caricato al 100%%!

:: --- 4. APERTURA BROWSER ---
ECHO [3/4] Apertura Interfaccia Web...
timeout /t 2 >nul
start http://localhost:8080

:: --- 5. AVVIO JAVA ---
ECHO [4/4] Avvio Backend Java...
ECHO.
ECHO -- IL SISTEMA E' PRONTO (Non chiudere questa finestra) --
:: ATTENZIONE: Questo comando punta al file nel percorso reale mostrato nelle tue foto
java -jar "backend-java\target\backend-java-1.0-SNAPSHOT.jar"

:: Quando chiudi Java, uccidi Python
taskkill /F /IM ml-service.exe >nul 2>&1