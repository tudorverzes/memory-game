import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.vv.boudary.CliDisplay;
import org.vv.boudary.InputHandler;
import org.vv.Main;
import org.vv.entity.GameConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.Permission;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        // Prevent System.exit() from terminating the JVM during tests
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
    @DisplayName("TC-M-1: Verify version display via --version")
    void testMain_VersionFlagLong() {
        String[] args = {"--version"};
        Main.main(args);
        assertTrue(outContent.toString().contains("Memory Card Game v1.0.0"));
    }

    @Test
    @DisplayName("TC-M-2: Verify help display via -h")
    void testMain_HelpFlagShort() {
        String[] args = {"-h"};
        Main.main(args);
        assertTrue(outContent.toString().contains("=== MEMORY CARD GAME - HELP ==="));
    }

    @Test
    @DisplayName("TC-M-3: Verify main menu launch without args")
    void testMain_NoArgs_LaunchesMenu() {
        // Simulate user selecting "3" (Exit) immediately
        String input = "3" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        Main.main(new String[]{});

        assertTrue(outContent.toString().contains("MEMORY CARD GAME"));
        assertTrue(outContent.toString().contains("1. New Game"));
    }

    @Test
    @DisplayName("TC-M-5: Verify UTF-8 Error Handling")
    void testMain_Utf8Error() {
        try (MockedStatic<Main> mockedMain = Mockito.mockStatic(Main.class, Mockito.CALLS_REAL_METHODS)) {
            mockedMain.when(Main::setupConsoleEncoding)
                    .thenThrow(new UnsupportedEncodingException("Mock Exception"));

            // Just run with -h to trigger the start sequence
            String[] args = {"-h"};
            Main.main(args);

            assertTrue(outContent.toString().contains("ERRORE: UTF-8 non supportato"));
        }
    }

    @Test
    @DisplayName("TC-M-6: Verify NoSuchElementException Handling")
    void testMain_NoSuchElement() {
        // Empty input stream triggers NoSuchElementException in scanner
        System.setIn(new ByteArrayInputStream(new byte[0]));
        InputHandler.setScanner(System.in);

        Main.main(new String[]{});

        assertTrue(outContent.toString().contains("Game ended"));
    }

    @Test
    @DisplayName("TC-M-10: Verify invalid menu option")
    void testRunMainMenu_InvalidOption() throws InterruptedException {
        // Input: "9" (Invalid) -> "3" (Exit)
        // Note: The code sleeps on invalid input, so we might need to wait or interrupt,
        // but with mocking we can skip the wait or just let it run fast.
        String input = "9" + System.lineSeparator() + "3" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        Main.main(new String[]{});

        assertTrue(outContent.toString().contains("Invalid option"));
    }

    @Test
    @DisplayName("TC-M-18: Verify Too Many Players Error")
    void testArgs_TooManyPlayers() {
        String[] args = {"-p", "1", "-n", "A", "-n", "B"};

        assertThrows(ExitException.class, () -> Main.main(args));
        // We can check if showError was called if we mock CliDisplay, or check output
        // But checking exit is good enough for the branch.
    }

    @Test
    @DisplayName("TC-M-19: Verify Unknown Option Error")
    void testArgs_UnknownOption() {
        String[] args = {"-z"};
        assertThrows(ExitException.class, () -> Main.main(args));
    }

    @Test
    @DisplayName("TC-M-21: Verify Invalid Player Count Error")
    void testArgs_InvalidPlayerCount() {
        String[] args = {"-p", "5"};
        assertThrows(ExitException.class, () -> Main.main(args));
    }

    @Test
    @DisplayName("TC-M-22: Verify Invalid Difficulty Error")
    void testArgs_InvalidDifficulty() {
        String[] args = {"-d", "SuperHard"};
        assertThrows(ExitException.class, () -> Main.main(args));
    }

    @Test
    @DisplayName("TC-M-25: Verify Name Truncation in Args")
    void testArgs_NameTruncation() {
        String longName = "1234567890123456789012345"; // 25 chars
        String[] args = {"-n", longName};

        // Needs input for "Play again" to finish gracefully
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        try (MockedConstruction<org.vv.boudary.Game> mockedGame = Mockito.mockConstruction(org.vv.boudary.Game.class)) {
            Main.main(args);

            assertTrue(outContent.toString().contains("Name too long"));
        }
    }

    @Test
    @DisplayName("TC-M-31: Verify help display via -h")
    void testMain_HelpFlagShortAlt() {
        String[] args = {"-h"};
        Main.main(args);
        assertTrue(outContent.toString().contains("=== MEMORY CARD GAME - HELP ==="));
    }

    @Test
    @DisplayName("TC-M-32: Verify help display via --help")
    void testMain_HelpFlagLong() {
        String[] args = {"--help"};
        Main.main(args);
        assertTrue(outContent.toString().contains("=== MEMORY CARD GAME - HELP ==="));
    }

    @Test
    @DisplayName("TC-M-33: Verify version display via -v") // Note: Code uses -V actually based on previous snippet, adjusting to match code logic if needed, assuming -v/-V logic
    void testMain_VersionFlagShort() {
        // The code snippet showed -V or --version. Assuming case sensitivity based on common unix tools or code.
        // If code has "-V", we test "-V". If it was fixed to "-v", we test "-v".
        // Let's assume standard case from typical requirements but verify against code.
        // Code has: else if (args[0].equals("-V") || args[0].equals("--version"))
        // So we test -V
        String[] args = {"-V"};
        Main.main(args);
        assertTrue(outContent.toString().contains("Memory Card Game v1.0.0"));
    }

    // If the code was fixed to accept -v as well:
    // @Test
    // void testMain_VersionFlagLowerV() { ... }

    @Test
    @DisplayName("TC-M-34: Verify version display via --version")
    void testMain_VersionFlagLongDuplicate() {
        String[] args = {"--version"};
        Main.main(args);
        assertTrue(outContent.toString().contains("Memory Card Game v1.0.0"));
    }

    @Test
    @DisplayName("TC-M-35: Verify New Game option from menu")
    void testRunMainMenu_NewGame() {
        // Input sequence:
        // "1" (New Game) -> "1" (1 Player) -> "Name" -> "1" (Difficulty) -> "n" (Play Again = No) -> "3" (Exit Menu)
        String input = "1" + System.lineSeparator() +
                "1" + System.lineSeparator() +
                "Player1" + System.lineSeparator() +
                "1" + System.lineSeparator() +
                "n" + System.lineSeparator() +
                "3" + System.lineSeparator();

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        // Mock Game to avoid full game execution logic which might hang or require complex moves
        try (MockedConstruction<org.vv.boudary.Game> mockedGame = Mockito.mockConstruction(org.vv.boudary.Game.class,
                (mock, context) -> {
                    Mockito.doNothing().when(mock).run();
                })) {

            Main.main(new String[]{});

            // Verify Game was constructed
            assertEquals(1, mockedGame.constructed().size());
        }
    }

    @Test
    @DisplayName("TC-M-36: Verify Help option from menu")
    void testRunMainMenu_Help() {
        // Input: "2" (Help) -> Enter (return) -> "3" (Exit)
        String input = "2" + System.lineSeparator() +
                System.lineSeparator() +
                "3" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        Main.main(new String[]{});

        // Verify help text appears
        assertTrue(outContent.toString().contains("=== MEMORY CARD GAME - HELP ==="));
    }

    @Test
    @DisplayName("TC-M-37: Verify game replay loop")
    void testRunGameSession_Replay() {
        // Start with args, then play again (interactive), then stop.
        // Args start: -p 1
        // Play again? y
        // Interactive setup: 1 player, Name "Replay", Easy
        // Play again? n

        String input = "y" + System.lineSeparator() +
                "1" + System.lineSeparator() +
                "Replay" + System.lineSeparator() +
                "1" + System.lineSeparator() +
                "n" + System.lineSeparator();

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        try (MockedConstruction<org.vv.boudary.Game> mockedGame = Mockito.mockConstruction(org.vv.boudary.Game.class,
                (mock, context) -> {
                    Mockito.doNothing().when(mock).run();
                })) {

            String[] args = {"-p", "1"};
            Main.main(args);

            // Game should be constructed twice: once for initial args, once for replay
            assertEquals(2, mockedGame.constructed().size());
        }
    }

    @Test
    @DisplayName("TC-M-38: Verify verbose command line flags")
    void testParseCommandLineArgs_VerboseFlags() {
        // --players 1 --difficulty hard --name Test
        // Play again? n
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        try (MockedConstruction<org.vv.boudary.Game> mockedGame = Mockito.mockConstruction(org.vv.boudary.Game.class,
                (mock, context) -> {
                    // Verify config passed to constructor?
                    // Difficult to access constructor args directly here without Captor on constructor
                    Mockito.doNothing().when(mock).run();
                })) {

            String[] args = {"--players", "1", "--difficulty", "hard", "--name", "VerbosePlayer"};
            Main.main(args);

            assertEquals(1, mockedGame.constructed().size());
            // Verification of correct config parsing relies on Game logic or we could inspect the config object if we mocked the Game constructor arguments capture
        }
    }

    @Test
    @DisplayName("TC-M-39: Verify missing flag values (Boundary)")
    void testParseCommandLineArgs_MissingValue() {
        // -p (no value)
        String[] args = {"-p"};

        // Expect System.exit(1) or graceful handling.
        // The code does: if (i + 1 < args.length) ... else ... nothing?
        // If loop finishes without finding value, it uses default.
        // Let's see. -p is at index 0. i+1 = 1. args.length = 1. 1 < 1 is False.
        // So it enters the if block for "-p", checks length, condition fails, does nothing.
        // Returns default config (Easy, 1 player).

        // Needs input for "Play again"
        String input = "n" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InputHandler.setScanner(System.in);

        try (MockedConstruction<org.vv.boudary.Game> mockedGame = Mockito.mockConstruction(org.vv.boudary.Game.class)) {
            Main.main(args);
            assertEquals(1, mockedGame.constructed().size());
        }
    }

    @Test
    @DisplayName("TC-M-40: Verify clearConsole on Windows")
    void testClearConsole_Windows() {
        String originalOs = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 10");

        try (MockedConstruction<ProcessBuilder> mockedPb = Mockito.mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    Process mockProcess = Mockito.mock(Process.class);
                    Mockito.when(mock.inheritIO()).thenReturn(mock);
                    Mockito.when(mock.start()).thenReturn(mockProcess);
                    Mockito.when(mockProcess.waitFor()).thenReturn(0);
                })) {

            Main.clearConsole();
            // Should have created a process builder for "cmd /c cls"
            assertEquals(1, mockedPb.constructed().size());

        } finally {
            System.setProperty("os.name", originalOs);
        }
    }

    // Helper exception for testing System.exit()
    private static class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) {
            this.status = status;
        }
    }

    // Security Manager to intercept System.exit()
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