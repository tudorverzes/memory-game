package boudary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.vv.boudary.CliDisplay;
import org.vv.entity.Card;
import org.vv.entity.ErrorMessages;
import org.vv.entity.GameBoard;
import org.vv.entity.GameState;
import org.vv.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CliDisplayTest {

	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private String originalOs;

	@BeforeEach
	void setUp() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
		originalOs = System.getProperty("os.name");
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
		System.setErr(originalErr);
		if (originalOs != null) {
			System.setProperty("os.name", originalOs);
		}
	}

	@Test
	@DisplayName("TC-CLI-01: Verify clearScreen() functionality on Windows")
	void testClearScreen_Windows() {
		System.setProperty("os.name", "Windows 10");

		try (MockedConstruction<ProcessBuilder> mockedPb = Mockito.mockConstruction(ProcessBuilder.class,
				(mock, context) -> {
					Process mockProcess = mock(Process.class);
					when(mock.inheritIO()).thenReturn(mock);
					when(mock.start()).thenReturn(mockProcess);
					when(mockProcess.waitFor()).thenReturn(0);
				})) {

			CliDisplay.clearScreen();

			assertEquals(1, mockedPb.constructed().size());
		}
	}

	@Test
	@DisplayName("TC-CLI-02: Verify clearScreen() handles exceptions")
	void testClearScreen_InterruptedException_CheckOutput() {
		System.setProperty("os.name", "Windows 10");

		try (MockedConstruction<ProcessBuilder> mockedPb = Mockito.mockConstruction(ProcessBuilder.class,
				(mock, context) -> {
					Process mockProcess = mock(Process.class);
					when(mock.inheritIO()).thenReturn(mock);
					when(mock.start()).thenReturn(mockProcess);
					when(mockProcess.waitFor()).thenThrow(new InterruptedException("Simulated Interruption"));
				})) {

			CliDisplay.clearScreen();

			assertTrue(outContent.toString().contains(ErrorMessages.E013));
		}
	}

	@Test
	@DisplayName("TC-CLI-03: Verify clearScreen() functionality on Linux")
	void testClearScreen_Linux() {
		System.setProperty("os.name", "Linux");

		CliDisplay.clearScreen();

		assertEquals("\033[H\033[2J", outContent.toString());
	}

	@Test
	@DisplayName("TC-CLI-04: Verify showHelp() prints correct usage instructions")
	void testShowHelp() {
		CliDisplay.showHelp();

		String output = outContent.toString();
		assertTrue(output.contains("=== MEMORY CARD GAME - HELP ==="));
		assertTrue(output.contains("How to play:"));
		assertTrue(output.contains("-p, --players [1|2]"));
		assertTrue(output.contains("java -jar memogame.jar"));
	}

	@Test
	@DisplayName("TC-CLI-05: Verify showVersion() prints correct version")
	void testShowVersion() {
		CliDisplay.showVersion();

		String output = outContent.toString().trim();
		assertEquals("Memory Card Game v1.0.0", output);
	}

	@Test
	@DisplayName("TC-CLI-06: Verify displayBoard() renders grid and scores correctly")
	void testDisplayBoard() {
		System.setProperty("os.name", "Linux");

		GameState mockState = mock(GameState.class);
		GameBoard mockBoard = mock(GameBoard.class);
		Player p1 = new Player("Alice");
		Player p2 = new Player("Bob");
		List<Player> players = Arrays.asList(p1, p2);

		Card hiddenCard = mock(Card.class);
		when(hiddenCard.isRevealed()).thenReturn(false);

		Card revealedCard = mock(Card.class);
		when(revealedCard.isRevealed()).thenReturn(true);
		when(revealedCard.getSymbol()).thenReturn("X");

		when(mockState.getBoard()).thenReturn(mockBoard);
		when(mockState.getPlayers()).thenReturn(players);
		when(mockState.getCurrentPlayerIndex()).thenReturn(0);

		when(mockBoard.getSize()).thenReturn(2);
		when(mockBoard.getRemainingPairs()).thenReturn(1);

		when(mockBoard.getCardAt(0, 0)).thenReturn(revealedCard);
		when(mockBoard.getCardAt(0, 1)).thenReturn(hiddenCard);
		when(mockBoard.getCardAt(1, 0)).thenReturn(hiddenCard);
		when(mockBoard.getCardAt(1, 1)).thenReturn(hiddenCard);

		CliDisplay.displayBoard(mockState);

		String output = outContent.toString();

		assertTrue(output.contains("--- Score ---"));
		assertTrue(output.contains("Alice: 0 couples <- CURRENT TURN"));
		assertTrue(output.contains("Bob: 0 couples"));
		assertTrue(output.contains("Remaining couples: 1"));
		assertTrue(output.contains("A"));
		assertTrue(output.contains("B"));
		assertTrue(output.contains("[X]"));
		assertTrue(output.contains("[?]"));
	}

	@Test
	@DisplayName("TC-CLI-07: Verify displayGameEnd() for Single Player victory")
	void testDisplayGameEnd_SinglePlayer() {
		System.setProperty("os.name", "Linux");

		GameState mockState = mock(GameState.class);
		GameBoard mockBoard = mock(GameBoard.class);
		Player p1 = new Player("SoloPlayer");
		List<Player> players = Arrays.asList(p1);

		Card mockCard = mock(Card.class);
		when(mockCard.isRevealed()).thenReturn(true);
		when(mockCard.getSymbol()).thenReturn("A");

		when(mockState.getBoard()).thenReturn(mockBoard);
		when(mockState.getPlayers()).thenReturn(players);
		when(mockState.getTotalTurns()).thenReturn(10);
		when(mockBoard.getSize()).thenReturn(1);
		when(mockBoard.getCardAt(anyInt(), anyInt())).thenReturn(mockCard);

		CliDisplay.displayGameEnd(mockState);

		String output = outContent.toString();
		assertTrue(output.contains("GAME COMPLETED!"));
		assertTrue(output.contains("SoloPlayer has completed the game in 10 round."));
	}

	@Test
	@DisplayName("TC-CLI-08: Verify displayGameEnd() for Multiplayer Draw")
	void testDisplayGameEnd_MultiPlayer_Draw() {
		System.setProperty("os.name", "Linux");

		GameState mockState = mock(GameState.class);
		GameBoard mockBoard = mock(GameBoard.class);
		Player p1 = new Player("Alice");
		Player p2 = new Player("Bob");

		p1.incrementScore();
		p2.incrementScore();
		List<Player> players = Arrays.asList(p1, p2);

		Card mockCard = mock(Card.class);
		when(mockCard.isRevealed()).thenReturn(true);
		when(mockCard.getSymbol()).thenReturn("A");

		when(mockState.getBoard()).thenReturn(mockBoard);
		when(mockState.getPlayers()).thenReturn(players);
		when(mockBoard.getSize()).thenReturn(1);
		when(mockBoard.getCardAt(anyInt(), anyInt())).thenReturn(mockCard);

		CliDisplay.displayGameEnd(mockState);

		String output = outContent.toString();
		assertTrue(output.contains("Final scores:"));
		assertTrue(output.contains("It's a draw!"));
	}

	@Test
	@DisplayName("TC-CLI-09: Verify displayGameEnd() for Multiplayer Winner")
	void testDisplayGameEnd_MultiPlayer_Winner() {
		System.setProperty("os.name", "Linux");

		GameState mockState = mock(GameState.class);
		GameBoard mockBoard = mock(GameBoard.class);
		Player p1 = new Player("Winner");
		Player p2 = new Player("Loser");

		p1.incrementScore();
		p1.incrementScore();
		p2.incrementScore();
		List<Player> players = Arrays.asList(p1, p2);

		Card mockCard = mock(Card.class);
		when(mockCard.isRevealed()).thenReturn(true);
		when(mockCard.getSymbol()).thenReturn("A");

		when(mockState.getBoard()).thenReturn(mockBoard);
		when(mockState.getPlayers()).thenReturn(players);
		when(mockBoard.getSize()).thenReturn(1);
		when(mockBoard.getCardAt(anyInt(), anyInt())).thenReturn(mockCard);

		CliDisplay.displayGameEnd(mockState);

		String output = outContent.toString();
		assertTrue(output.contains("Winner: 2 couples"));
		assertTrue(output.contains("Winner wins!"));
	}

	@Test
	@DisplayName("TC-CLI-10: Verify showError() prints correct message")
	void testShowError() {
		String errorMessage = "Critical system failure";
		CliDisplay.showError(errorMessage);

		assertEquals(errorMessage + System.lineSeparator(), outContent.toString());
	}

	@Test
	@DisplayName("TC-CLI-11: Verify instantiation")
	void testConstructor() {
		CliDisplay cliDisplay = new CliDisplay();
		assertNotNull(cliDisplay);
	}
}