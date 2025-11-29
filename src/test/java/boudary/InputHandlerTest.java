package boudary;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.vv.boudary.InputHandler;
import org.vv.entity.Card;
import org.vv.entity.Coordinate;
import org.vv.entity.Difficulty;
import org.vv.entity.ErrorMessages;
import org.vv.entity.GameBoard;
import org.vv.exception.MaxAttemptsExceededException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InputHandlerTest {

	private final InputStream originalIn = System.in;
	private static final PrintStream originalOut = System.out;
	private static ByteArrayOutputStream outputStream;

        @Mock
    private GameBoard mockBoard;
    @Mock
    private Card mockCardA1;
    @Mock
    private Card mockCardA2;

    @BeforeEach
    void setUp() throws Exception {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void restoreStreams() {
        
        System.setIn(originalIn);
        System.setOut(originalOut);
        
        InputHandler.setScanner(originalIn);
    }

	private void provideInput(String data) {
		ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
		System.setIn(testIn);
		InputHandler.setScanner(testIn);
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

        String consoleOutput = outputStream.toString();
        // No WARNING expected. Prompts are OK.
        assertFalse(consoleOutput.contains("Name too long"), "Unexpected warning printed");
        assertFalse(consoleOutput.contains("error"), "Unexpected error printed");
    }

    @Test
    @DisplayName("TC-INP-02: Trimmed valid input returns name without spaces")
    void testGetPlayerName_validTrimmedInput() throws Exception {
        setScannerInput("   Alice   \n");

        String result = InputHandler.getPlayerName(1, "Default");

        assertEquals("Alice", result);

        String consoleOutput = outputStream.toString();
        assertFalse(consoleOutput.contains("Name too long"), "Unexpected warning printed");
        assertFalse(consoleOutput.contains("error"), "Unexpected error printed");
    }

    @Test
    @DisplayName("TC-INP-03: Too-long name is truncated and emits a warning")
    void testGetPlayerName_nameTooLong_isTruncatedWithWarning() throws Exception {
        String longName = "abcdefghijklmnopqrstuXYZ";
        setScannerInput(longName + "\n");
        String result = InputHandler.getPlayerName(1, "DefaultName");
		String consoleOutput = outputStream.toString();
		assertTrue(consoleOutput.contains("Name too long"));
		assertEquals(20, result.length());
		assertEquals("abcdefghijklmnopqrst", result);
	}

	    @Test
    void testGetPlayerName_EmptyInput_ReturnsDefault() {
        provideInput("\n");
        String result = InputHandler.getPlayerName(1, "Player 1");
        assertEquals("Player 1", result);
    }

    @Test
    void testGetPlayerName_ValidInput_ReturnsName() {
        provideInput("Alice\n");
        String result = InputHandler.getPlayerName(1, "Player 1");
        assertEquals("Alice", result);
    }

    @Test
    void testGetPlayerName_TooLong_Truncates() {
		
        String longName = "AlessandroMagnoIlConquistatore"; 
        provideInput(longName + "\n");
        
        String result = InputHandler.getPlayerName(1, "Default");
        
        assertEquals(20, result.length());
        assertEquals("AlessandroMagnoIlCon", result);
		
        assertTrue(outputStream.toString().contains("Name too long"));
    }

	
    @Test
    void testGetPlayerCount_Valid_1() {
        provideInput("1\n");
        assertEquals(1, InputHandler.getPlayerCount());
    }

    @Test
    void testGetPlayerCount_Valid_2() {
        provideInput("2\n");
        assertEquals(2, InputHandler.getPlayerCount());
    }

    @Test
    void testGetPlayerCount_InvalidNumber_ThenValid() {
		
        provideInput("3\n1\n");
        assertEquals(1, InputHandler.getPlayerCount());
        assertTrue(outputStream.toString().contains(ErrorMessages.E009));
    }

    @Test
    void testGetPlayerCount_InvalidFormat_ThenValid() {
		
        provideInput("abc\n2\n");
        assertEquals(2, InputHandler.getPlayerCount());
        assertTrue(outputStream.toString().contains(ErrorMessages.E005));
    }

    @Test
    void testGetPlayerCount_MaxAttemptsExceeded() {
		
        provideInput("3\n3\n3\n3\n3\n");
        assertThrows(MaxAttemptsExceededException.class, () -> {
            InputHandler.getPlayerCount();
        });
    }

	
    @Test
    void testGetDifficulty_Easy() {
        provideInput("1\n");
        assertEquals(Difficulty.EASY, InputHandler.getDifficulty());
    }

    @Test
    void testGetDifficulty_Medium() {
        provideInput("2\n");
        assertEquals(Difficulty.MEDIUM, InputHandler.getDifficulty());
    }

    @Test
    void testGetDifficulty_Hard() {
        provideInput("3\n");
        assertEquals(Difficulty.HARD, InputHandler.getDifficulty());
    }

    @Test
    void testGetDifficulty_Invalid_ThenValid() {
		
        provideInput("4\n1\n");
        assertEquals(Difficulty.EASY, InputHandler.getDifficulty());
        assertTrue(outputStream.toString().contains(ErrorMessages.E005));
    }
    
    @Test
    void testGetDifficulty_MaxAttempts() {
        provideInput("x\nx\nx\nx\nx\n");
        assertThrows(MaxAttemptsExceededException.class, () -> {
           InputHandler.getDifficulty(); 
        });
    }

	
    @Test
    void testGetCoordinateInput_Success() {
        provideInput("A1\n");
        int gridSize = 4;
        
		
        Coordinate expected = new Coordinate(0, 0);

		
        when(mockBoard.getCardAt(eq(expected))).thenReturn(mockCardA1);
        when(mockCardA1.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt:", gridSize, mockBoard, null);
        assertEquals(expected, result);
    }

    @Test
    void testGetCoordinateInput_CardAlreadyRevealed() {
    	provideInput("A1\nA2\n");
        int gridSize = 4;
        Coordinate expectedResult = new Coordinate(1, 0); 


        	when(mockBoard.getCardAt(any(Coordinate.class)))
            .thenReturn(mockCardA1)
            .thenReturn(mockCardA2);

        when(mockCardA1.isRevealed()).thenReturn(true);
        when(mockCardA2.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt:", gridSize, mockBoard, null);
        
        assertEquals(expectedResult.getRow(), result.getRow());
        assertEquals(expectedResult.getCol(), result.getCol());

        assertTrue(outputStream.toString().contains(ErrorMessages.E003));
    }

    @Test
    void testGetCoordinateInput_SameCardSelected() {
        provideInput("A1\nA2\n");
        int gridSize = 4;

        Coordinate firstChoice = new Coordinate(0, 0);
        Coordinate expectedResult = new Coordinate(1, 0);

        	when(mockBoard.getCardAt(any(Coordinate.class)))
             .thenReturn(mockCardA1)
             .thenReturn(mockCardA2);

        when(mockCardA1.isRevealed()).thenReturn(false);
        when(mockCardA2.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt:", gridSize, mockBoard, firstChoice);

        assertEquals(expectedResult.getRow(), result.getRow());
        assertEquals(expectedResult.getCol(), result.getCol());
        
        assertTrue(outputStream.toString().contains(ErrorMessages.E002));
    }

    @Test
    void testGetCoordinateInput_InvalidFormat() {
		
        provideInput("1A\nA1\n"); 
        int gridSize = 4;
        Coordinate expected = new Coordinate(0, 0);
        when(mockBoard.getCardAt(eq(expected))).thenReturn(mockCardA1);
        when(mockCardA1.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt:", gridSize, mockBoard, null);
        
        assertEquals(expected, result);
		
    }

    @Test
    void testGetCoordinateInput_OutOfBounds() {
		provideInput("Z9\nA1\n");
        int gridSize = 4;
        Coordinate expected = new Coordinate(0, 0);

        when(mockBoard.getCardAt(eq(expected))).thenReturn(mockCardA1);
        when(mockCardA1.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt:", gridSize, mockBoard, null);
        
        assertEquals(expected, result);}
    
    @Test
    void testGetCoordinateInput_IllegalArgument() {

        provideInput(" \nA1\n");
        int gridSize = 4;
        Coordinate expected = new Coordinate(0, 0);

        when(mockBoard.getCardAt(eq(expected))).thenReturn(mockCardA1);
        when(mockCardA1.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt:", gridSize, mockBoard, null);
        assertEquals(expected, result);
    }

    @Test
    void testGetPlayAgain_Yes() {
        provideInput("y\n");
        assertTrue(InputHandler.getPlayAgain());
    }
    
    @Test
    void testGetPlayAgain_YesFull() {
        provideInput("yes\n");
        assertTrue(InputHandler.getPlayAgain());
    }

    @Test
    void testGetPlayAgain_No() {
        provideInput("n\n");
        assertFalse(InputHandler.getPlayAgain());
    }
    
    @Test
    void testGetPlayAgain_Invalid_ThenNo() {
        provideInput("bho\nn\n");
        assertFalse(InputHandler.getPlayAgain());
        assertTrue(outputStream.toString().contains(ErrorMessages.E005));
    }

    @Test
    void testGetMenuOption() {
        provideInput("1\n");
        assertEquals("1", InputHandler.getMenuOption());
    }
    
    @Test
    void testCloseScanner() {
		
		InputHandler.closeScanner();
    }
    
    @Test
    void testGetMenuOption_ThrowsNoSuchElementException() {
        // 1. Crea un input stream VUOTO (0 byte)
        // Questo simula che il flusso di input sia finito o chiuso.
        ByteArrayInputStream emptyInput = new ByteArrayInputStream(new byte[0]);

        // 2. Iniettalo usando il tuo metodo setScanner
        InputHandler.setScanner(emptyInput);

        // 3. Verifica che venga lanciata l'eccezione
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            InputHandler.getMenuOption();
        });

        // 4. Verifica che il messaggio sia quello personalizzato
        assertEquals("Input stream closed.", exception.getMessage());
    }
/*
    @Test
    void testNameTooLong() {

	
        String result = InputHandler.getPlayerName(1, "Default");

        assertEquals("abcdefghijklmnopqrst", result);

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Name too long"), "Expected truncation warning");
    }*/
}
