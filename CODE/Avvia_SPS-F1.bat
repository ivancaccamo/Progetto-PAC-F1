echo off
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

:: --- 3. LOADER GRAFICO (JAVA SWING) ---
ECHO [3/4] Generazione Loader Grafico (Rosso e Nero)...

:: Mi assicuro di essere nella cartella giusta
CD /D "%~dp0"

:: Pulisco vecchi file
IF EXIST Loader.java DEL Loader.java
IF EXIST Loader.class DEL Loader.class

:: SCRITTURA FILE (Stile F1 Racing: Nero e Rosso - No System LookAndFeel)
ECHO import javax.swing.*; >> Loader.java
ECHO import javax.swing.plaf.basic.BasicProgressBarUI; >> Loader.java
ECHO import java.awt.*; >> Loader.java
ECHO import java.net.*; >> Loader.java
ECHO public class Loader { >> Loader.java
ECHO      public static void main(String[] args) { >> Loader.java
ECHO          JFrame frame = new JFrame(); >> Loader.java
ECHO          frame.setUndecorated(true); >> Loader.java
ECHO          frame.setSize(420, 100); >> Loader.java
ECHO          frame.setLocationRelativeTo(null); >> Loader.java
ECHO          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); >> Loader.java
ECHO          JPanel panel = new JPanel(); >> Loader.java
ECHO          panel.setBackground(new Color(18, 18, 18)); >> Loader.java
ECHO          panel.setLayout(new BorderLayout()); >> Loader.java
ECHO          panel.setBorder(BorderFactory.createLineBorder(new Color(220, 20, 60), 2)); >> Loader.java
ECHO          JLabel label = new JLabel("AVVIO SISTEMA SPS-F1...", JLabel.CENTER); >> Loader.java
ECHO          label.setForeground(Color.WHITE); >> Loader.java
ECHO          label.setFont(new Font("Segoe UI", Font.BOLD, 14)); >> Loader.java
ECHO          label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0)); >> Loader.java
ECHO          JProgressBar bar = new JProgressBar(); >> Loader.java
ECHO          bar.setForeground(new Color(220, 20, 60)); >> Loader.java
ECHO          bar.setBackground(new Color(50, 50, 50)); >> Loader.java
ECHO          bar.setBorderPainted(false); >> Loader.java
ECHO          bar.setIndeterminate(true); >> Loader.java
ECHO          bar.setPreferredSize(new Dimension(300, 8)); >> Loader.java
ECHO          JPanel barPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); >> Loader.java
ECHO          barPanel.setBackground(new Color(18, 18, 18)); >> Loader.java
ECHO          barPanel.add(bar); >> Loader.java
ECHO          panel.add(label, BorderLayout.NORTH); >> Loader.java
ECHO          panel.add(barPanel, BorderLayout.CENTER); >> Loader.java
ECHO          frame.add(panel); >> Loader.java
ECHO          frame.setVisible(true); >> Loader.java
ECHO          boolean connected = false; >> Loader.java
ECHO          while (!connected) { >> Loader.java
ECHO              try { >> Loader.java
ECHO                  Socket s = new Socket("127.0.0.1", 5000); >> Loader.java
ECHO                  connected = true; >> Loader.java
ECHO                  s.close(); >> Loader.java
ECHO              } catch (Exception e) { >> Loader.java
ECHO                  try { Thread.sleep(500); } catch (Exception ie) {} >> Loader.java
ECHO              } >> Loader.java
ECHO          } >> Loader.java
ECHO          frame.dispose(); >> Loader.java
ECHO          System.exit(0); >> Loader.java
ECHO      } >> Loader.java
ECHO } >> Loader.java

:: COMPILAZIONE ED ESECUZIONE
javac Loader.java
IF %ERRORLEVEL% EQU 0 (
    start /WAIT java Loader
    DEL Loader.java
    DEL Loader.class
) ELSE (
    ECHO [ERRORE] Javac ha fallito la compilazione.
    PAUSE
)

:: --- 4. APERTURA BROWSER ---
ECHO [3/4] Apertura Interfaccia Web...
timeout /t 2 >nul
start http://localhost:8080

:: --- 5. AVVIO JAVA ---
ECHO [4/4] Avvio Backend Java...
ECHO.
ECHO -- IL SISTEMA E' PRONTO --
ECHO Se il browser da errore, aspetta 10 secondi e ricarica la pagina.

java -jar "backend-java\target\backend-java-1.0-SNAPSHOT.jar"

:: AGGIUNGI QUESTO:
IF %ERRORLEVEL% NEQ 0 (
    ECHO [ERRORE] Il server Java e' crashato. Leggi l'errore sopra.
    PAUSE
)

:: Quando chiudi Java (se va tutto bene), uccidi Python
taskkill /F /IM ml-service.exe >nul 2>&1

:: Quando chiudi Java, uccidi Python
taskkill /F /IM ml-service.exe >nul 2>&1