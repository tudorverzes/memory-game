package boudary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vv.boudary.CliDisplay;
import org.vv.boudary.Game;
import org.vv.boudary.InputHandler;
import org.vv.entity.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Mock
    private GameState mockState;
    @Mock
    private GameBoard mockBoard;
    @Mock
    private Player mockPlayer1;
    @Mock
    private Player mockPlayer2;
    @Mock
    private Card mockCard1;
    @Mock
    private Card mockCard2;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void injectMockState(Game game, GameState state) throws Exception {
        Field field = Game.class.getDeclaredField("state");
        field.setAccessible(true);
        field.set(game, state);
    }

    @Test
    @DisplayName("TC-GAME-01: Verify player name assignment when all names are provided in configuration")
    void testConstructor_WithProvidedNames() throws Exception {
        List<String> names = Arrays.asList("Alice", "Bob");
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 2, names);

        Game game = new Game(config);

        Field field = Game.class.getDeclaredField("state");
        field.setAccessible(true);
        GameState state = (GameState) field.get(game);

        List<Player> players = state.getPlayers();
        assertEquals(2, players.size());
        assertEquals("Alice", players.get(0).getName());
        assertEquals("Bob", players.get(1).getName());
    }

    @Test
    @DisplayName("TC-GAME-02: Verify default player name generation when names are missing")
    void testConstructor_WithMissingNames() throws Exception {
        List<String> names = Collections.singletonList("Alice");
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 2, names);

        Game game = new Game(config);

        Field field = Game.class.getDeclaredField("state");
        field.setAccessible(true);
        GameState state = (GameState) field.get(game);

        List<Player> players = state.getPlayers();
        assertEquals(2, players.size());
        assertEquals("Alice", players.get(0).getName());
        assertEquals("Player 2", players.get(1).getName());
    }

    @Test
    @DisplayName("TC-GAME-03: Verify turn counter increments only for single-player games")
    void testPlayTurn_SinglePlayer_IncrementsTurns() throws Exception {
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 1, Collections.singletonList("Player1"));
        Game game = new Game(config);
        injectMockState(game, mockState);

        when(mockState.isComplete()).thenReturn(false).thenReturn(true);
        when(mockState.getPlayers()).thenReturn(Collections.singletonList(mockPlayer1));
        when(mockState.getCurrentPlayer()).thenReturn(mockPlayer1);
        when(mockPlayer1.getName()).thenReturn("Player1");
        when(mockState.getBoard()).thenReturn(mockBoard);
        when(mockBoard.getSize()).thenReturn(4);

        when(mockCard1.getId()).thenReturn("id1");
        when(mockCard2.getId()).thenReturn("id2");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard1).thenReturn(mockCard2);

        try (MockedStatic<InputHandler> inputMock = mockStatic(InputHandler.class);
             MockedStatic<CliDisplay> cliMock = mockStatic(CliDisplay.class)) {

            inputMock.when(() -> InputHandler.getCoordinateInput(anyString(), anyInt(), any(), any()))
                    .thenReturn(new Coordinate(0, 0))
                    .thenReturn(new Coordinate(0, 1));

            game.run();

            verify(mockState, atLeastOnce()).incrementTotalTurns();
        }
    }

    @Test
    @DisplayName("TC-GAME-04: Verify turn counter does not increment for multiplayer games")
    void testPlayTurn_MultiPlayer_NoIncrement() throws Exception {
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 2, Arrays.asList("P1", "P2"));
        Game game = new Game(config);
        injectMockState(game, mockState);

        when(mockState.isComplete()).thenReturn(false).thenReturn(true);
        when(mockState.getPlayers()).thenReturn(Arrays.asList(mockPlayer1, mockPlayer2));
        when(mockState.getCurrentPlayer()).thenReturn(mockPlayer1);
        when(mockPlayer1.getName()).thenReturn("P1");
        when(mockState.getBoard()).thenReturn(mockBoard);
        when(mockBoard.getSize()).thenReturn(4);

        when(mockCard1.getId()).thenReturn("id1");
        when(mockCard2.getId()).thenReturn("id2");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard1).thenReturn(mockCard2);

        try (MockedStatic<InputHandler> inputMock = mockStatic(InputHandler.class);
             MockedStatic<CliDisplay> cliMock = mockStatic(CliDisplay.class)) {

            inputMock.when(() -> InputHandler.getCoordinateInput(anyString(), anyInt(), any(), any()))
                    .thenReturn(new Coordinate(0, 0))
                    .thenReturn(new Coordinate(0, 1));

            game.run();

            verify(mockState, never()).incrementTotalTurns();
        }
    }

    @Test
    @DisplayName("TC-GAME-05: Verify logic when a matching pair of cards is selected")
    void testPlayTurn_MatchFound() throws Exception {
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 1, Collections.singletonList("P1"));
        Game game = new Game(config);
        injectMockState(game, mockState);

        when(mockState.isComplete()).thenReturn(false).thenReturn(true);
        when(mockState.getPlayers()).thenReturn(Collections.singletonList(mockPlayer1));
        when(mockState.getCurrentPlayer()).thenReturn(mockPlayer1);
        when(mockPlayer1.getName()).thenReturn("P1");
        when(mockState.getBoard()).thenReturn(mockBoard);

        when(mockCard1.getId()).thenReturn("match");
        when(mockCard2.getId()).thenReturn("match");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard1).thenReturn(mockCard2);

        try (MockedStatic<InputHandler> inputMock = mockStatic(InputHandler.class);
             MockedStatic<CliDisplay> cliMock = mockStatic(CliDisplay.class)) {

            inputMock.when(() -> InputHandler.getCoordinateInput(anyString(), anyInt(), any(), any()))
                    .thenReturn(new Coordinate(0, 0));

            game.run();

            verify(mockPlayer1).incrementScore();
            verify(mockBoard).recordMatch();
            assertTrue(outContent.toString().contains("Match found!"));
        }
    }

    @Test
    @DisplayName("TC-GAME-06: Verify logic when a non-matching pair is selected")
    void testPlayTurn_NoMatch() throws Exception {
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 1, Collections.singletonList("P1"));
        Game game = new Game(config);
        injectMockState(game, mockState);

        when(mockState.isComplete()).thenReturn(false).thenReturn(true);
        when(mockState.getPlayers()).thenReturn(Collections.singletonList(mockPlayer1));
        when(mockState.getCurrentPlayer()).thenReturn(mockPlayer1);
        when(mockPlayer1.getName()).thenReturn("P1");
        when(mockState.getBoard()).thenReturn(mockBoard);

        when(mockCard1.getId()).thenReturn("id1");
        when(mockCard2.getId()).thenReturn("id2");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard1).thenReturn(mockCard2);

        try (MockedStatic<InputHandler> inputMock = mockStatic(InputHandler.class);
             MockedStatic<CliDisplay> cliMock = mockStatic(CliDisplay.class)) {

            inputMock.when(() -> InputHandler.getCoordinateInput(anyString(), anyInt(), any(), any()))
                    .thenReturn(new Coordinate(0, 0));

            game.run();

            verify(mockCard1).setRevealed(false);
            verify(mockCard2).setRevealed(false);
            verify(mockState).nextTurn();
            assertTrue(outContent.toString().contains("No match found. Retry."));
        }
    }

    @Test
    @DisplayName("TC-GAME-07: Verify handling of InterruptedException during the non-match delay")
    void testPlayTurn_InterruptedException() throws Exception {
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 1, Collections.singletonList("P1"));
        Game game = new Game(config);
        injectMockState(game, mockState);

        when(mockState.isComplete()).thenReturn(false).thenReturn(true);
        when(mockState.getPlayers()).thenReturn(Collections.singletonList(mockPlayer1));
        when(mockState.getCurrentPlayer()).thenReturn(mockPlayer1);
        when(mockPlayer1.getName()).thenReturn("P1");
        when(mockState.getBoard()).thenReturn(mockBoard);

        when(mockCard1.getId()).thenReturn("id1");
        when(mockCard2.getId()).thenReturn("id2");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard1).thenReturn(mockCard2);

        try (MockedStatic<InputHandler> inputMock = mockStatic(InputHandler.class);
             MockedStatic<CliDisplay> cliMock = mockStatic(CliDisplay.class)) {

            inputMock.when(() -> InputHandler.getCoordinateInput(anyString(), anyInt(), any(), any()))
                    .thenReturn(new Coordinate(0, 0));

            Thread testThread = new Thread(game::run);
            testThread.start();

            Thread.sleep(100);
            testThread.interrupt();
            testThread.join(2000);
        }

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Pause interrupted"));
    }

    @Test
    @DisplayName("TC-GAME-08: Verify game completion detection and loop termination")
    void testRun_GameCompletion() throws Exception {
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 1, Collections.singletonList("P1"));
        Game game = new Game(config);
        injectMockState(game, mockState);

        when(mockState.isComplete()).thenReturn(false).thenReturn(false).thenReturn(true);

        when(mockState.getPlayers()).thenReturn(Collections.singletonList(mockPlayer1));
        when(mockState.getCurrentPlayer()).thenReturn(mockPlayer1);
        when(mockState.getBoard()).thenReturn(mockBoard);

        when(mockCard1.getId()).thenReturn("match");
        when(mockCard2.getId()).thenReturn("match");
        when(mockBoard.getCardAt(any(Coordinate.class))).thenReturn(mockCard1).thenReturn(mockCard2);

        when(mockBoard.areAllMatched()).thenReturn(true);

        try (MockedStatic<InputHandler> inputMock = mockStatic(InputHandler.class);
             MockedStatic<CliDisplay> cliMock = mockStatic(CliDisplay.class)) {

            inputMock.when(() -> InputHandler.getCoordinateInput(anyString(), anyInt(), any(), any()))
                    .thenReturn(new Coordinate(0, 0));

            game.run();

            verify(mockState).setComplete(true);
            cliMock.verify(() -> CliDisplay.displayGameEnd(mockState));
        }
    }
}