import org.junit.jupiter.api.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private static final String JAVA_BIN = System.getProperty("java.home") + "/bin/java";
    private static final String CLASSPATH = System.getProperty("java.class.path");
    private static final String MAIN_CLASS = "org.vv.Main";

    /**
     * Helper method to run the Main application in a separate process.
     *
     * @param inputs Inputs to be fed into System.in (for interactive modes). Pass null or empty for non-interactive.
     * @param args   Command line arguments to pass to the application.
     * @return The Process object.
     */
    private Process runApplication(String inputs, String... args) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(JAVA_BIN);
        command.add("-cp");
        command.add(CLASSPATH);
        command.add(MAIN_CLASS);
        if (args != null) {
            command.addAll(List.of(args));
        }

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // Merge stderr into stdout for easier assertion
        Process process = builder.start();

        // CRITICAL FIX: Always create and close the writer.
        // This ensures the OutputStream connected to the process's System.in is closed,
        // sending an EOF signal. Without this, scanner.nextLine() in the app hangs forever.
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            if (inputs != null) {
                writer.write(inputs);
            }
        } catch (IOException e) {
            // Ignore broken pipe errors if process exits early (e.g. invalid args triggering System.exit)
        }

        return process;
    }

    private String readOutput(Process process) throws IOException {
        return new String(process.getInputStream().readAllBytes());
    }

    // =================================================================================
    // PRIORITY FLAGS
    // =================================================================================

    @Test
    @DisplayName("TC-MAIN-01: Verify high-priority handling of the help flag")
    void testHelpFlagPriority() throws Exception {
        Process process = runApplication(null, "-h");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "Application should exit successfully with -h");
        assertTrue(output.contains("=== MEMORY CARD GAME - HELP ==="), "Should display help header");
        assertTrue(output.contains("-p, --players"), "Should display player option help");
    }

    @Test
    @DisplayName("TC-MAIN-02: Verify high-priority handling of the version flag")
    void testVersionFlagPriority() throws Exception {
        Process process = runApplication(null, "--version");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "Application should exit successfully with --version");
        assertTrue(output.contains("Memory Card Game v1.0.0"), "Should display correct version");
    }

    // =================================================================================
    // COMMAND LINE ARGUMENT PARSING (Happy Path & Exceptions)
    // =================================================================================

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("TC-MAIN-03: Verify parsing of a valid, fully specified command argument set")
    void testValidFullArguments() throws Exception {
        // The EOF signal sent by runApplication will cause the game loop to catch NoSuchElementException
        // and exit gracefully with code 0.
        Process process = runApplication(null, "-p", "2", "-d", "hard", "-n", "Alice", "-n", "Bob");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "Game should start and exit gracefully on EOF");
        assertTrue(output.contains("Game starts...Good luck!"), "Game should indicate start");
        assertTrue(output.contains("Alice"), "Player Alice should be initialized");
        assertTrue(output.contains("Bob"), "Player Bob should be initialized");
        // Hard mode is 8x8, so we look for the number 8 or coordinate H in the grid output logic
        assertTrue(output.contains(" 8 "), "Grid should display 8 rows for Hard difficulty");
    }

    @Test
    @DisplayName("TC-MAIN-04: Verify E009 error path for invalid integer player count (3)")
    void testInvalidPlayerCountInteger() throws Exception {
        Process process = runApplication(null, "-p", "3");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertNotEquals(0, exitCode, "Should exit with error code");
        assertTrue(output.contains("Invalid number of players"), "Should display E009 message");
    }

    @Test
    @DisplayName("TC-MAIN-05: Verify E009 exception handling for non-numeric player count")
    void testNonNumericPlayerCount() throws Exception {
        Process process = runApplication(null, "-p", "two");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertNotEquals(0, exitCode, "Should exit with error code");
        assertTrue(output.contains("Invalid number of players"), "Should display E009 message (via NumberFormatException)");
    }

    @Test
    @DisplayName("TC-MAIN-06: Verify E008 exception handling for invalid difficulty")
    void testInvalidDifficulty() throws Exception {
        Process process = runApplication(null, "-d", "extreme");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertNotEquals(0, exitCode, "Should exit with error code");
        assertTrue(output.contains("Invalid difficulty level"), "Should display E008 message");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("TC-MAIN-07: Verify name truncation logic for names exceeding 20 characters")
    void testNameTruncation() throws Exception {
        String longName = "Supercalifragilisticexpialidocious";
        Process process = runApplication(null, "-n", longName);

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "Game should start despite warning");
        assertTrue(output.contains("Name too long"), "Should display truncation warning");

        // Logic cuts to 20 chars
        String expectedTruncated = longName.substring(0, 20);
        assertTrue(output.contains(expectedTruncated), "Should contain truncated name");
        // Ensure the score section uses the truncated name
        assertTrue(output.contains(expectedTruncated + ": 0"), "Scoreboard should use truncated name");
    }

    @Test
    @DisplayName("TC-MAIN-08: Verify E007 logical validation for name count exceeding player count")
    void testTooManyNames() throws Exception {
        Process process = runApplication(null, "-p", "1", "-n", "Alice", "-n", "Bob");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertNotEquals(0, exitCode, "Should exit with error code");
        assertTrue(output.contains("Too many player names entered"), "Should display E007 message");
    }

    @Test
    @DisplayName("TC-MAIN-09: Verify E010 error handling for unrecognized flags")
    void testUnknownFlag() throws Exception {
        Process process = runApplication(null, "--godmode");

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertNotEquals(0, exitCode, "Should exit with error code");
        assertTrue(output.contains("Unknown option"), "Should display E010 message");
        assertTrue(output.contains("--godmode"), "Should specify which flag was unknown");
    }

    // =================================================================================
    // INTERACTIVE MAIN MENU FLOWS
    // =================================================================================

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("TC-MAIN-11: Verify 'Exit' flow in the interactive Main Menu")
    void testInteractiveMenuExit() throws Exception {
        // Simulate user typing "3" then Enter
        String input = "3\n";
        Process process = runApplication(input); // No args triggers interactive menu

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
        assertTrue(output.contains("MEMORY CARD GAME"), "Should show menu header");
        assertTrue(output.contains("3. Exit"), "Should show exit option");
        assertTrue(output.contains("Thank you for playing!"), "Should print exit message");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("TC-MAIN-12: Verify 'Help' flow handling within the interactive Main Menu")
    void testInteractiveMenuHelp() throws Exception {
        // Simulate user typing "2" (Help), then Enter (to return), then "3" (Exit)
        String input = "2\n\n3\n";
        Process process = runApplication(input);

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
        assertTrue(output.contains("How to play:"), "Should display help content");
        assertTrue(output.contains("Press Enter to return to menu..."), "Should prompt to return");
        assertTrue(output.contains("Thank you for playing!"), "Should eventually exit");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("TC-MAIN-13: Verify invalid input handling in the interactive Main Menu")
    void testInteractiveMenuInvalidInput() throws Exception {
        // Simulate user typing "9" (Invalid), then "3" (Exit)
        String input = "9\n3\n";
        Process process = runApplication(input);

        String output = readOutput(process);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode);
        assertTrue(output.contains("Invalid option. Please select 1, 2, or 3."), "Should warn on invalid input");
        assertTrue(output.contains("Thank you for playing!"), "Should eventually exit");
    }
}
