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

        System.setErr(new PrintStream(errContent));

        org.vv.boudary.InputHandler.setScanner(System.in);

        System.setSecurityManager(new NoExitSecurityManager());

    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
        System.setSecurityManager(null);
    }

    @Test
    void testMain_NoSuchElementException_GameEnded() {
    	
    	ByteArrayInputStream emptyInput = new ByteArrayInputStream("".getBytes());
        System.setIn(emptyInput);
        
        InputHandler.setScanner(System.in);
        System.setIn(new ByteArrayInputStream("".getBytes()));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        Main.main(new String[]{});

        String output = outContent.toString();
        assertTrue(output.contains("Game ended"), 
            "Output atteso: 'Game ended.' - Output ricevuto: " + output);
    }

    @Test
    void testMain_Utf8Error_ThenShowVersion() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            
            mockedMain.when(Main::setupConsoleEncoding)
                      .thenThrow(new UnsupportedEncodingException("Errore Simulato"));

            String[] args = {"--version"};
            Main.main(args);
        }

        // 3. VERIFICA
        String output = outContent.toString();

        assertTrue(output.contains("ERRORE: UTF-8 non supportato"),
                "Manca il messaggio di errore encoding. Output: " + output);

        assertFalse(output.isEmpty());
        assertTrue(output.contains("Memory Card Game v1.0.0"));
    }

    @Test
    void testCatchNumberFormatException_WithMockito() {
        System.setErr(new PrintStream(new ByteArrayOutputStream()));

        String[] args = {"-p", "ciao"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            
            mockedMain.when(() -> Main.exit(Mockito.anyInt()))
                      .thenAnswer(invocation -> null);

            Main.main(args);

            mockedMain.verify(() -> Main.exit(1));
        }
    }
    @Test
    void testCatchGenericException_WithMockito() {
        System.setErr(new PrintStream(new ByteArrayOutputStream()));

        String[] args = { "-p", "1", null }; 

        // 3. MOCKITO
        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            
            mockedMain.when(() -> Main.exit(Mockito.anyInt()))
                      .thenAnswer(invocation -> null);

            Main.main(args);

            mockedMain.verify(() -> Main.exit(1));
        }
    }
    
    @Test
    void testRunMainMenu_InterruptedException() throws InterruptedException {
        String simulatedInput = "9" + System.lineSeparator() + "3" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        Thread mainThread = new Thread(() -> {
            Main.main(new String[]{});
        });

        mainThread.start();

        Thread.sleep(400);

        mainThread.interrupt();

        mainThread.join(2000);
        String output = outContent.toString();
        assertTrue(output.contains("MEMORY CARD GAME"), 
            "Output atteso: 'Game ended.' - Output ricevuto: " + output);
    }

    @Test
    void testArgs_TooManyNames() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-p", "1", "-n", "nic", "-n", "sab", "-d", "easy"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
        	
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            mockedMain.verify(() -> Main.exit(1));
            
            String combinedOutput = outContent.toString();
            assertTrue(combinedOutput.contains("Too many player names entered. Only one name is required for single-player mode.") || combinedOutput.contains("Too many names supplied"),
                    "Dovrebbe stampare errore E007");
        }
    }

    @Test
    void testArgs_UnknownFlag() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-g"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            mockedMain.verify(() -> Main.exit(1));
            
            assertTrue(outContent.toString().contains("E010") || outContent.toString().contains("-g"),
                    "Dovrebbe segnalare opzione sconosciuta E010");
        }
    }


    @Test
    void testArgs_ValidPlayerCount() {
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);

        String[] args = {"-p", "1"};

        assertDoesNotThrow(() -> Main.main(args));
    }

    @Test
    void testArgs_InvalidPlayerCount_Logic() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-p", "3"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            mockedMain.verify(() -> Main.exit(1));
            assertTrue(outContent.toString().contains("Invalid number of players. Please choose 1 or 2."), "Dovrebbe dare errore E009 per numero giocatori errato");
        }
    }


    @Test
    void testArgs_ValidDifficulty() {
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);

        String[] args = {"-d", "easy"};

        assertDoesNotThrow(() -> Main.main(args));
    }

    @Test
    void testArgs_InvalidDifficulty() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"-d", "prova"};

        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(() -> Main.exit(Mockito.anyInt())).thenAnswer(i -> null);

            Main.main(args);

            mockedMain.verify(() -> Main.exit(1));
            assertTrue(outContent.toString().contains("Invalid difficulty level. Please choose: easy, medium, or hard."), "Dovrebbe dare errore E008 per difficoltà errata");
        }
    }
    
    @Test
    void testArgs_ValidName() {

        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);

        String[] args = {"-p", "1", "-n", "prova"};

        assertDoesNotThrow(() -> Main.main(args));
    }


    @Test
    void testArgs_LongNameTruncation() {

        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        org.vv.boudary.InputHandler.setScanner(System.in);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String longName = "prova" + "a".repeat(100);
        String[] args = {"-p", "1", "-n", longName};


        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {

            
            Main.main(args);

            String output = outContent.toString();
            assertTrue(output.contains("Name too long"), "Dovrebbe avvisare che il nome è troppo lungo");
            assertTrue(output.contains("truncated"), "Dovrebbe dire che è stato troncato");
        }
    }
    
    @Test
    void testClearConsole_Linux() {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String originalOs = System.getProperty("os.name");

        try {

            System.setProperty("os.name", "Linux");


            Main.clearConsole();


            assertEquals("\033[H\033[2J", outContent.toString());

        } finally {

            System.setProperty("os.name", originalOs);
        }
    }

    @Test
    void testClearConsole_Exception() {

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        String originalOs = System.getProperty("os.name");

        try {

            System.setProperty("os.name", "Windows 10");


            Process mockProcess = Mockito.mock(Process.class);
            Mockito.when(mockProcess.waitFor()).thenThrow(new InterruptedException("Interruzione Simulata"));

            try (MockedConstruction<ProcessBuilder> mockedPb = Mockito.mockConstruction(ProcessBuilder.class,
                    (mock, context) -> {
                        Mockito.when(mock.inheritIO()).thenReturn(mock);
                        Mockito.when(mock.start()).thenReturn(mockProcess);
                    })) {


                Main.clearConsole();
            }

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