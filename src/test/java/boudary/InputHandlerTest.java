package boudary;

import org.junit.jupiter.api.*;
import org.vv.boudary.InputHandler;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InputHandlerTest {

	private static final PrintStream originalOut = System.out;
	private static ByteArrayOutputStream outputStream;

	@BeforeEach
	void setUp() throws Exception {
		outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	private void setScannerInput(String simulatedInput) throws Exception {
		InputStream testInput = new ByteArrayInputStream(simulatedInput.getBytes());
		Field scannerField = InputHandler.class.getDeclaredField("scanner");
		scannerField.setAccessible(true);
		scannerField.set(null, new Scanner(testInput));
	}

	@Test
	@DisplayName("TC-INP-01: Empty input returns the default player name")
	void testGetPlayerName_emptyInput_returnsDefault() throws Exception {
		setScannerInput("\n");

		String result = InputHandler.getPlayerName(1, "DefaultName");

		assertEquals("DefaultName", result);
		assertTrue(outputStream.toString().isEmpty(), "No warnings expected");
	}

	@Test
	@DisplayName("TC-INP-02: Trimmed valid input returns name without spaces")
	void testGetPlayerName_validTrimmedInput() throws Exception {
		setScannerInput("   Alice   \n");

		String result = InputHandler.getPlayerName(1, "Default");

		assertEquals("Alice", result);
		assertTrue(outputStream.toString().isEmpty(), "No warnings expected");
	}

	@Test
	@DisplayName("TC-INP-03: Too-long name is truncated and emits a warning")
	void testGetPlayerName_nameTooLong_isTruncatedWithWarning() throws Exception {
		String longName = "abcdefghijklmnopqrstuXYZ";
		setScannerInput(longName + "\n");

		String result = InputHandler.getPlayerName(1, "Default");

		assertEquals("abcdefghijklmnopqrst", result);

		String consoleOutput = outputStream.toString();
		assertTrue(consoleOutput.contains("Name too long"), "Expected truncation warning");
	}
}
