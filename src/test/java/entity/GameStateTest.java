package entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.vv.entity.Difficulty;
import org.vv.entity.GameBoard;
import org.vv.entity.GameState;
import org.vv.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    @Mock
    private GameBoard mockBoard;
    @Mock
    private Player mockPlayer1;
    @Mock
    private Player mockPlayer2;
    @Mock
    private Difficulty mockDifficulty;

    private List<Player> singlePlayerList;
    private List<Player> twoPlayerList;

    @BeforeEach
    void setUp() {
        singlePlayerList = Collections.singletonList(mockPlayer1);
        twoPlayerList = List.of(mockPlayer1, mockPlayer2);
    }

    @Test
    @DisplayName("[TC-GS-01] Should initialize with correct default state")
    void testGameStateInitialization() {
        GameState state = new GameState(mockBoard, twoPlayerList, mockDifficulty);

        assertAll("Initial State Verification",
                () -> assertSame(mockBoard, state.getBoard(), "Should store the provided GameBoard."),
                () -> assertSame(twoPlayerList, state.getPlayers(), "Should store the provided player list."),
                () -> assertSame(mockDifficulty, state.getDifficulty(), "Should store the provided Difficulty."),
                () -> assertEquals(0, state.getCurrentPlayerIndex(), "Current player index should start at 0."),
                () -> assertEquals(0, state.getTotalTurns(), "Total turns should start at 0."),
                () -> assertFalse(state.isComplete(), "Game should not be complete on initialization.")
        );
    }

    @Test
    @DisplayName("[TC-GS-02] getCurrentPlayer() should return the first player initially")
    void testGetCurrentPlayerAtStart() {
        GameState state = new GameState(mockBoard, twoPlayerList, mockDifficulty);

        Player currentPlayer = state.getCurrentPlayer();

        assertSame(mockPlayer1, currentPlayer, "The initial current player should be the one at index 0.");
    }

    @Test
    @DisplayName("[TC-GS-03] incrementTotalTurns() should increase turn count by one")
    void testIncrementTotalTurns() {
        GameState state = new GameState(mockBoard, twoPlayerList, mockDifficulty);
        assertEquals(0, state.getTotalTurns(), "Precondition: Total turns is 0.");

        state.incrementTotalTurns();
        int turnsAfterOneIncrement = state.getTotalTurns();
        state.incrementTotalTurns();
        int turnsAfterTwoIncrements = state.getTotalTurns();

        assertEquals(1, turnsAfterOneIncrement, "Turns should be 1 after one increment.");
        assertEquals(2, turnsAfterTwoIncrements, "Turns should be 2 after two increments.");
    }

    @Test
    @DisplayName("[TC-GS-04] setComplete() should update the completion status")
    void testSetComplete() {
        GameState state = new GameState(mockBoard, twoPlayerList, mockDifficulty);
        assertFalse(state.isComplete(), "Precondition: Game is not complete.");

        state.setComplete(true);

        assertTrue(state.isComplete(), "Game should be complete after setComplete(true).");

        state.setComplete(false);
        assertFalse(state.isComplete(), "Game should be incomplete after setComplete(false).");
    }

    @Test
    @DisplayName("[TC-GS-05] nextTurn() should cycle players in a two-player game")
    void testNextTurnForTwoPlayers() {
        GameState state = new GameState(mockBoard, twoPlayerList, mockDifficulty);

        assertEquals(0, state.getCurrentPlayerIndex());
        assertSame(mockPlayer1, state.getCurrentPlayer());

        state.nextTurn();
        assertEquals(1, state.getCurrentPlayerIndex());
        assertSame(mockPlayer2, state.getCurrentPlayer());

        state.nextTurn();
        assertEquals(0, state.getCurrentPlayerIndex());
        assertSame(mockPlayer1, state.getCurrentPlayer());
    }

    @Test
    @DisplayName("[TC-GS-06] nextTurn() should not change player in a single-player game")
    void testNextTurnForSinglePlayer() {
        GameState state = new GameState(mockBoard, singlePlayerList, mockDifficulty);
        assertEquals(0, state.getCurrentPlayerIndex(), "Precondition: Index is 0.");

        state.nextTurn();
        // Assert after one turn
        assertEquals(0, state.getCurrentPlayerIndex(), "Index should remain 0 after one turn.");
        assertSame(mockPlayer1, state.getCurrentPlayer());

        state.nextTurn();
        // Assert after second turn
        assertEquals(0, state.getCurrentPlayerIndex(), "Index should remain 0 after a second turn.");
        assertSame(mockPlayer1, state.getCurrentPlayer());
    }

    @Test
    @DisplayName("[TC-GS-07] Should throw NullPointerException with null player list")
    void testBehaviorWithNullPlayerList() {
        GameState state = new GameState(mockBoard, null, mockDifficulty);

        assertThrows(NullPointerException.class, state::getCurrentPlayer,
                "getCurrentPlayer should throw NullPointerException on a null player list.");
        assertThrows(NullPointerException.class, state::nextTurn,
                "nextTurn should throw NullPointerException on a null player list.");
    }

    @Test
    @DisplayName("[TC-GS-08] Should throw exceptions with empty player list")
    void testBehaviorWithEmptyPlayerList() {
        GameState state = new GameState(mockBoard, new ArrayList<>(), mockDifficulty);

        assertThrows(IndexOutOfBoundsException.class, state::getCurrentPlayer,
                "getCurrentPlayer should throw IndexOutOfBoundsException on an empty player list.");
        assertThrows(ArithmeticException.class, state::nextTurn,
                "nextTurn should throw ArithmeticException (division by zero) on an empty player list.");
    }
}
