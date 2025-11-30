package boudary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vv.boudary.CliDisplay;
import org.vv.boudary.InputHandler;
import org.vv.entity.Card;
import org.vv.entity.Coordinate;
import org.vv.entity.Difficulty;
import org.vv.entity.ErrorMessages;
import org.vv.entity.GameBoard;
import org.vv.exception.MaxAttemptsExceededException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InputHandlerTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    private GameBoard mockBoard;
    @Mock
    private Card mockCard;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private void setInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InputHandler.setScanner(in);
    }

    @Test
    @DisplayName("TC-INP-01: Verify empty input returns default name")
    void testGetPlayerName_Empty() {
        setInput("\n");
        String result = InputHandler.getPlayerName(1, "Default");
        assertEquals("Default", result);
    }

    @Test
    @DisplayName("TC-INP-02: Verify valid trimmed input returns name")
    void testGetPlayerName_Valid() {
        setInput("   Alice   \n");
        String result = InputHandler.getPlayerName(1, "Default");
        assertEquals("Alice", result);
    }

    @Test
    @DisplayName("TC-INP-03: Verify long name truncation")
    void testGetPlayerName_Truncation() {
        String longName = "ThisNameIsWayTooLongToBeAcceptedByType";
        setInput(longName + "\n");
        String result = InputHandler.getPlayerName(1, "Default");
        assertEquals("ThisNameIsWayTooLong", result);
        assertTrue(outContent.toString().contains("Name too long"));
    }

    @Test
    @DisplayName("TC-INP-04: Verify valid coordinate input")
    void testGetCoordinateInput_Success() {
        setInput("A1\n");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard);
        when(mockCard.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt", 4, mockBoard, null);
        assertEquals(new Coordinate(0, 0), result);
    }

    @Test
    @DisplayName("TC-INP-05: Verify retry on revealed card")
    void testGetCoordinateInput_RevealedRetry() {
        setInput("A1\nA2\n");
        when(mockBoard.getCardAt(new Coordinate(0, 0))).thenReturn(mockCard);
        when(mockCard.isRevealed()).thenReturn(true);

        Card unrevealedCard = org.mockito.Mockito.mock(Card.class);
        when(mockBoard.getCardAt(new Coordinate(1, 0))).thenReturn(unrevealedCard);
        when(unrevealedCard.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt", 4, mockBoard, null);

        assertEquals(new Coordinate(1, 0), result);
        assertTrue(outContent.toString().contains(ErrorMessages.E003));
    }

    @Test
    @DisplayName("TC-INP-06: Verify retry on same card selection")
    void testGetCoordinateInput_SameCardRetry() {
        setInput("A1\nA2\n");
        Coordinate first = new Coordinate(0, 0);

        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard);
        when(mockCard.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt", 4, mockBoard, first);

        assertEquals(new Coordinate(1, 0), result);
        assertTrue(outContent.toString().contains(ErrorMessages.E002));
    }

    @Test
    @DisplayName("TC-INP-07: Verify retry on invalid formats")
    void testGetCoordinateInput_InvalidFormats() {
        setInput("1A\nZ9\nA1\n");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard);
        when(mockCard.isRevealed()).thenReturn(false);

        Coordinate result = InputHandler.getCoordinateInput("Prompt", 4, mockBoard, null);

        assertEquals(new Coordinate(0, 0), result);

        String output = outContent.toString();
        assertTrue(output.contains(ErrorMessages.E004) || output.contains(ErrorMessages.E001));
    }

    @Test
    @DisplayName("TC-INP-08: Verify stream closed exception")
    void testGetMenuOption_StreamClosed() {
        setInput("");
        assertThrows(NoSuchElementException.class, InputHandler::getMenuOption);
    }

    @Test
    @DisplayName("TC-INP-09: Verify difficulty selection")
    void testGetDifficulty_Success() {
        setInput("1\n2\n3\n");
        assertEquals(Difficulty.EASY, InputHandler.getDifficulty());
        assertEquals(Difficulty.MEDIUM, InputHandler.getDifficulty());
        assertEquals(Difficulty.HARD, InputHandler.getDifficulty());
    }

    @Test
    @DisplayName("TC-INP-10: Verify max attempts exceeded for difficulty")
    void testGetDifficulty_MaxAttempts() {
        setInput("x\n4\n0\nabc\n9\n");
        assertThrows(MaxAttemptsExceededException.class, InputHandler::getDifficulty);
        assertTrue(outContent.toString().contains(ErrorMessages.E005));
    }

    @Test
    @DisplayName("TC-INP-11: Verify player count valid inputs")
    void testGetPlayerCount_Valid() {
        setInput("1\n2\n");
        assertEquals(1, InputHandler.getPlayerCount());
        assertEquals(2, InputHandler.getPlayerCount());
    }

    @Test
    @DisplayName("TC-INP-12: Verify player count invalid inputs")
    void testGetPlayerCount_Invalid() {
        setInput("3\nabc\n1\n");
        assertEquals(1, InputHandler.getPlayerCount());
        String output = outContent.toString();
        assertTrue(output.contains(ErrorMessages.E009));
        assertTrue(output.contains(ErrorMessages.E005));
    }

    @Test
    @DisplayName("TC-INP-13: Verify play again logic")
    void testGetPlayAgain() {
        setInput("y\nyes\nn\nno\nx\nn\n");
        assertTrue(InputHandler.getPlayAgain());
        assertTrue(InputHandler.getPlayAgain());
        assertFalse(InputHandler.getPlayAgain());
        assertFalse(InputHandler.getPlayAgain());
        assertFalse(InputHandler.getPlayAgain());
        assertTrue(outContent.toString().contains(ErrorMessages.E005));
    }

    @Test
    @DisplayName("TC-INP-14: Verify close scanner")
    void testCloseScanner() {
        assertDoesNotThrow(InputHandler::closeScanner);
    }
}