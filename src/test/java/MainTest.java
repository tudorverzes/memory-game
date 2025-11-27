import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.vv.boudary.InputHandler;
import org.vv.Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.Permission;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();



    @BeforeEach
    public void setUp() {

        // Redirigiamo System.err per catturare i messaggi di errore senza sporcare la console

        System.setErr(new PrintStream(errContent));

        org.vv.boudary.InputHandler.setScanner(System.in);

        // Installiamo il blocco per impedire System.exit()

        System.setSecurityManager(new NoExitSecurityManager());

    }

    @AfterEach
    public void tearDown() {
        // Ripristiniamo SEMPRE gli stream e il SecurityManager dopo ogni test
        // per evitare che un test rompa quello successivo.
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
        System.setSecurityManager(null);
    }

    @Test
    void testMain_NoSuchElementException_GameEnded() {
    	
    	ByteArrayInputStream emptyInput = new ByteArrayInputStream("".getBytes());
        System.setIn(emptyInput);
        
        // AGGIORNA LO SCANNER SUBITO DOPO AVER CAMBIATO L'INPUT
        InputHandler.setScanner(System.in);
        // 1. SETUP STREAM
        // Input vuoto -> Genera NoSuchElementException appena prova a leggere
        System.setIn(new ByteArrayInputStream("".getBytes()));

        // Catturiamo l'output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // 2. ESECUZIONE
        Main.main(new String[]{});

        // 3. VERIFICA
        // Se il catch nel main funziona, deve stampare "Game ended."
        String output = outContent.toString();
        assertTrue(output.contains("Game ended"), 
            "Output atteso: 'Game ended.' - Output ricevuto: " + output);
    }

    @Test
    void testMain_Utf8Error_ThenShowVersion() {
        // 1. SETUP STREAM (PRIMA di eseguire il main!)
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // 2. MOCKING E ESECUZIONE
        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            
            // Sabotiamo il metodo che imposta l'encoding
            mockedMain.when(Main::setupConsoleEncoding)
                      .thenThrow(new UnsupportedEncodingException("Errore Simulato"));

            // Eseguiamo il main con il flag versione
            String[] args = {"--version"};
            Main.main(args);
        }

        // 3. VERIFICA
        String output = outContent.toString();

        // Verifica A: Errore encoding catturato
        assertTrue(output.contains("ERRORE: UTF-8 non supportato"),
                "Manca il messaggio di errore encoding. Output: " + output);

        // Verifica B: Versione mostrata (assicurati che CliDisplay stampi davvero questa stringa)
        // Se non sai cosa stampa, controlla solo che l'output non sia vuoto o che non ci siano errori
        assertFalse(output.isEmpty());
        assertTrue(output.contains("Memory Card Game v1.0.0"));
    }

    @Test
    void testCatchNumberFormatException_WithMockito() {
        // Zittiamo l'errore su console
        System.setErr(new PrintStream(new ByteArrayOutputStream()));

        String[] args = {"-p", "ciao"};

        // Usiamo Mockito per "spiara" Main
        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            
            // DISINNESCO: Quando viene chiamato Main.exit(qualsiasi numero), NON fare nulla.
            // Questo permette al codice di passare oltre la riga 'exit(1)' colorandola di verde.
            mockedMain.when(() -> Main.exit(Mockito.anyInt()))
                      .thenAnswer(invocation -> null); // Non fare nulla (void)

            // ESECUZIONE
            // Non lancerà più eccezioni, il codice "crederà" di essere uscito ma continuerà.
            Main.main(args);

            // VERIFICA
            // Verifichiamo che Main.exit(1) sia stato effettivamente chiamato 1 volta
            mockedMain.verify(() -> Main.exit(1));
        }
    }
    @Test
    void testCatchGenericException_WithMockito() {
        // 1. SETUP: Zittiamo l'errore su console per pulizia
        System.setErr(new PrintStream(new ByteArrayOutputStream()));

        // 2. INPUT "KILLER": 
        // "-p" e "1" sono ok, ma 'null' farà esplodere il ciclo for con una NullPointerException.
        String[] args = { "-p", "1", null }; 

        // 3. MOCKITO
        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            
            // DISINNESCO: Quando il codice chiama Main.exit(...), non fare nulla.
            // Questo permette a JaCoCo di segnare la riga come "eseguita".
            mockedMain.when(() -> Main.exit(Mockito.anyInt()))
                      .thenAnswer(invocation -> null);

            // ESECUZIONE
            // Il main verrà eseguito, catturerà la NullPointerException nel blocco "catch (Exception e)",
            // chiamerà exit(1) (che noi abbiamo disattivato) e proseguirà.
            Main.main(args);

            // VERIFICA
            // Ci assicuriamo che il programma abbia effettivamente provato a uscire con codice 1
            mockedMain.verify(() -> Main.exit(1));
        }
    }
    
    @Test
    void testRunMainMenu_InterruptedException() throws InterruptedException {
        // 1. SETUP INPUT
        // Inviamo "9" (opzione non valida) per entrare nel 'default' e far scattare la sleep.
        // Inviamo "3" (exit) subito dopo, per permettere al programma di uscire dal ciclo while 
        // dopo che l'eccezione è stata gestita.
        String simulatedInput = "9" + System.lineSeparator() + "3" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Catturiamo l'output per evitare di sporcare la console (opzionale)
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // 2. CREAZIONE DEL THREAD
        // Eseguiamo il Main in un thread parallelo, altrimenti il test rimarrebbe bloccato 
        // ad aspettare la fine della sleep senza poter fare nulla.
        Thread mainThread = new Thread(() -> {
            Main.main(new String[]{});
        });

        // 3. AVVIO
        mainThread.start();

        // 4. ATTESA SINCRONIZZATA
        // Aspettiamo un tempo sufficiente (es. 400ms) affinché il thread:
        // a) Legga "9"
        // b) Stampi "Invalid option..."
        // c) Entri dentro TimeUnit.SECONDS.sleep(1)
        Thread.sleep(400);

        // 5. INTERRUZIONE (L'AZIONE CHIAVE)
        // Mentre il mainThread sta dormendo (ha ancora circa 600ms di sonno),
        // noi lo interrompiamo. Questo forza la sleep a lanciare InterruptedException.
        mainThread.interrupt();

        // 6. CHIUSURA
        // Aspettiamo che il thread finisca di eseguire (leggerà "3" ed uscirà)
        // Usiamo un timeout di sicurezza per non bloccare il test se qualcosa va storto.
        mainThread.join(2000);
        String output = outContent.toString();
        assertTrue(output.contains("MEMORY CARD GAME"), 
            "Output atteso: 'Game ended.' - Output ricevuto: " + output);
        // Se il test arriva qui senza errori e il thread è terminato, 
        // la coverage segnerà il blocco catch come eseguito.
    }
    
 // -------------------------------------------------------------------------
    // TRADUZIONE DEI COMANDI MANUALI IN JUNIT
    // -------------------------------------------------------------------------

    /**
     * Comando originale:
     * java ... -p 1 -n nic -n sab -d easy
     * Obiettivo: Verificare errore logico (troppi nomi per 1 giocatore).
     */
    @Test
    void testArgs_TooManyNames() {
        // Redirigi output ed errori
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-p", "1", "-n", "nic", "-n", "sab", "-d", "easy"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            // Disinneschiamo l'exit
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            // Verifica: Deve aver chiamato exit(1) perché ci sono 2 nomi per 1 giocatore
            mockedMain.verify(() -> Main.exit(1));
            
            // Verifica: Deve aver stampato l'errore E007 (Too many names)
            // Nota: Controlliamo sia out che err per sicurezza
            String combinedOutput = outContent.toString();
            // Assumiamo che ErrorMessages.E007 contenga il testo dell'errore
            // Se non hai accesso a ErrorMessages nel test, cerca parte della stringa
            assertTrue(combinedOutput.contains("Too many player names entered. Only one name is required for single-player mode.") || combinedOutput.contains("Too many names supplied"),
                    "Dovrebbe stampare errore E007");
        }
    }

    /**
     * Comando originale:
     * java ... -g
     * Obiettivo: Verificare flag sconosciuto.
     */
    @Test
    void testArgs_UnknownFlag() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-g"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            // Verifica: exit(1) per opzione invalida
            mockedMain.verify(() -> Main.exit(1));
            
            // Verifica messaggio E010
            assertTrue(outContent.toString().contains("E010") || outContent.toString().contains("-g"),
                    "Dovrebbe segnalare opzione sconosciuta E010");
        }
    }

    /**
     * Comando originale:
     * java ... -p 1
     * Obiettivo: Setup valido minimo.
     */
    @Test
    void testArgs_ValidPlayerCount() {
        // SIMULIAMO INPUT UTENTE: "n" per dire NO al play again e uscire dal loop
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);

        String[] args = {"-p", "1"};

        assertDoesNotThrow(() -> Main.main(args));
    }

    /**
     * Comando originale:
     * java ... -p 3
     * Obiettivo: Numero giocatori non valido (logica <1 o >2).
     */
    @Test
    void testArgs_InvalidPlayerCount_Logic() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-p", "3"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            // Verifica: exit(1)
            mockedMain.verify(() -> Main.exit(1));
            assertTrue(outContent.toString().contains("Invalid number of players. Please choose 1 or 2."), "Dovrebbe dare errore E009 per numero giocatori errato");
        }
    }

    /**
     * Comando originale:
     * java ... -d easy
     * Obiettivo: Setup valido solo difficoltà.
     */
    @Test
    void testArgs_ValidDifficulty() {
        // Input "n" per uscire dal gioco
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);

        String[] args = {"-d", "easy"};

        assertDoesNotThrow(() -> Main.main(args));
    }

    /**
     * Comando originale:
     * java ... -d prova
     * Obiettivo: Difficoltà inesistente (IllegalArgumentException).
     */
    @Test
    void testArgs_InvalidDifficulty() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-d", "prova"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            // Verifica: exit(1) e errore E008
            mockedMain.verify(() -> Main.exit(1));
            assertTrue(outContent.toString().contains("Invalid difficulty level. Please choose: easy, medium, or hard."), "Dovrebbe dare errore E008 per difficoltà errata");
        }
    }

    /**
     * Comando originale:
     * java ... -p 1 -n prova
     * Obiettivo: Setup valido completo.
     */
    @Test
    void testArgs_ValidName() {
        // Input "n" per uscire dal gioco
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);

        String[] args = {"-p", "1", "-n", "prova"};

        assertDoesNotThrow(() -> Main.main(args));
    }

    /**
     * Comando originale:
     * java ... -p 1 -n provaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
     * Obiettivo: Troncamento nome lungo.
     */
    @Test
    void testArgs_LongNameTruncation() {
        // Input "n" per uscire dal gioco
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String longName = "prova" + "a".repeat(100);
        String[] args = {"-p", "1", "-n", longName};

        // Qui NON deve uscire, ma deve stampare un warning
        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            // Se chiama exit è un errore, quindi non lo mockiamo per disinnescarlo, 
            // ma verifichiamo che NON venga chiamato (o meglio, lasciamo che il gioco parta).
            // Tuttavia, per sicurezza usiamo assertDoesNotThrow.
            
            Main.main(args);
            
            // Verifica che abbia stampato l'avviso
            String output = outContent.toString();
            assertTrue(output.contains("Name too long"), "Dovrebbe avvisare che il nome è troppo lungo");
            assertTrue(output.contains("truncated"), "Dovrebbe dire che è stato troncato");
        }
    }
    
    @Test
    void testClearConsole_Linux() {
        // 1. SETUP: Catturiamo l'output e salviamo l'OS originale
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String originalOs = System.getProperty("os.name");

        try {
            // 2. FORZIAMO L'AMBIENTE LINUX
            System.setProperty("os.name", "Linux");

            // 3. ESECUZIONE
            Main.clearConsole();

            // 4. VERIFICA
            // Ci aspettiamo la sequenza di escape ANSI esatta
            assertEquals("\033[H\033[2J", outContent.toString());

        } finally {
            // 5. CLEANUP
            System.setProperty("os.name", originalOs);
        }
    }

    /**
     * TEST 2: Caso Windows (Ramo If)
     * Verifica che venga lanciato il comando "cmd /c cls".
     * Usiamo Mockito per intercettare la creazione del processo senza eseguirlo davvero.
     */
 
    /**
     * TEST 3: Caso Eccezione (Ramo Catch)
     * Simuliamo un errore durante l'attesa del processo per vedere se stampa su System.err.
     */
    @Test
    void testClearConsole_Exception() {
        // 1. SETUP STREAM ERRORI
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String originalOs = System.getProperty("os.name");

        try {
            // Forziamo Windows per entrare nel blocco try che lancia eccezioni
            System.setProperty("os.name", "Windows 10");

            // 2. SABOTAGGIO
            // Creiamo un processo che lancia InterruptedException quando si fa waitFor()
            Process mockProcess = Mockito.mock(Process.class);
            Mockito.when(mockProcess.waitFor()).thenThrow(new InterruptedException("Interruzione Simulata"));

            try (MockedConstruction<ProcessBuilder> mockedPb = Mockito.mockConstruction(ProcessBuilder.class,
                    (mock, context) -> {
                        Mockito.when(mock.inheritIO()).thenReturn(mock);
                        Mockito.when(mock.start()).thenReturn(mockProcess);
                    })) {

                // 3. ESECUZIONE
                Main.clearConsole();
            }

            // 4. VERIFICA
            // Controlliamo che abbia catturato l'errore e stampato il messaggio
            String outputErr = errContent.toString();
            assertTrue(outputErr.contains("Errore durante la pulizia della console"),
                    "Dovrebbe stampare il messaggio di errore nel catch.");
            assertTrue(outputErr.contains("Interruzione Simulata"));

        } catch (Exception e) {
            fail("Il test non dovrebbe lanciare eccezioni, ma gestirle.");
        } finally {
            System.setProperty("os.name", originalOs);
        }
    }


    private static class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) {
            this.status = status;
        }
    }

    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) { }
        @Override
        public void checkPermission(Permission perm, Object context) { }
        @Override
        public void checkExit(int status) {
            throw new ExitException(status);
        }
    }
}