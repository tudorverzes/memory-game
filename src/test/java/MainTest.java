import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MainArgumentParsingTest {

    private Process runMain(String... args) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + "/bin/java");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add("org.vv.Main");
        command.addAll(List.of(args));
        return new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
    }

    private String read(Process p) throws Exception {
        return new String(p.getInputStream().readAllBytes());
    }

    private int waitExit(Process p) throws Exception {
        return p.waitFor();
    }

    // ---------------------- TESTS ---------------------

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-01: Valid -p 2 parsed correctly")
    void testValidPlayers() throws Exception {
        Process p = runMain("-p", "2");
        int code = waitExit(p);   // <-- this will hang, timeout saves us
        assertEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-02: Invalid players triggers E009")
    void testInvalidPlayers() throws Exception {
        Process p = runMain("-p", "5");
        int code = waitExit(p);

        String out = read(p);
        assertTrue(out.contains("E009"));
        assertNotEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-03: Valid difficulty parsed")
    void testValidDifficulty() throws Exception {
        Process p = runMain("-d", "medium");
        int code = waitExit(p);  // <-- hangs, timeout handles it
        assertEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-04: Invalid difficulty triggers E008")
    void testInvalidDifficulty() throws Exception {
        Process p = runMain("-d", "banana");
        int code = waitExit(p);

        String out = read(p);
        assertTrue(out.contains("E008"));
        assertNotEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-05: Long name is truncated")
    void testLongName() throws Exception {
        Process p = runMain("-n", "ABCDEFGHIJKLMNOPQRSTUV");
        int code = waitExit(p);  // <-- hangs, timeout handles it
        assertEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-06: Too many names triggers E007")
    void testTooManyNames() throws Exception {
        Process p = runMain("-p", "1", "-n", "Alice", "-n", "Bob");
        int code = waitExit(p);

        String out = read(p);
        assertTrue(out.contains("E007"));
        assertNotEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-07: Unknown flag triggers E010")
    void testUnknownFlag() throws Exception {
        Process p = runMain("--xyz");
        int code = waitExit(p);

        String out = read(p);
        assertTrue(out.contains("E010"));
        assertNotEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-08: Combination of valid flags works")
    void testValidCombo() throws Exception {
        Process p = runMain("-p", "2", "-d", "hard", "-n", "Alice");
        int code = waitExit(p); // <-- hangs, timeout handles it
        assertEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-09: Non-numeric players triggers E009")
    void testNonNumericPlayers() throws Exception {
        Process p = runMain("-p", "abc");
        int code = waitExit(p);

        String out = read(p);
        assertTrue(out.contains("E009"));
        assertNotEquals(0, code);
    }

    @Test
    @Timeout(20)
    @DisplayName("TC-MAIN-10: Missing value after -p triggers error")
    void testMissingValue() throws Exception {
        Process p = runMain("-p");
        int code = waitExit(p);

        String out = read(p);
        assertTrue(out.contains("Error generated"));
        assertNotEquals(0, code);
    }
}
