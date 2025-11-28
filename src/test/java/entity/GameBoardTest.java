package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vv.entity.Card;
import org.vv.entity.Coordinate;
import org.vv.entity.Difficulty;
import org.vv.entity.GameBoard;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardTest {
    @Test
    @DisplayName("[TC-GB-01] Should initialize correctly for Easy difficulty")
    void testEasyBoardInitialization() {
        GameBoard board = new GameBoard(Difficulty.EASY);

        assertEquals(4, board.getSize(), "Board size should be 4 for Easy difficulty.");
        assertEquals(8, board.getRemainingPairs(), "Initial remaining pairs should be 8 for Easy difficulty.");
        assertFalse(board.areAllMatched(), "A new board should not have all pairs matched.");
    }

    @Test
    @DisplayName("[TC-GB-02] Should initialize correctly for Medium difficulty")
    void testMediumBoardInitialization() {
        GameBoard board = new GameBoard(Difficulty.MEDIUM);

        assertEquals(6, board.getSize(), "Board size should be 6 for Medium difficulty.");
        assertEquals(18, board.getRemainingPairs(), "Initial remaining pairs should be 18 for Medium difficulty.");
        assertFalse(board.areAllMatched(), "A new board should not have all pairs matched.");
    }

    @Test
    @DisplayName("[TC-GB-03] Should initialize correctly for Hard difficulty")
    void testHardBoardInitialization() {
        GameBoard board = new GameBoard(Difficulty.HARD);

        assertEquals(8, board.getSize(), "Board size should be 8 for Hard difficulty.");
        assertEquals(32, board.getRemainingPairs(), "Initial remaining pairs should be 32 for Hard difficulty.");
        assertFalse(board.areAllMatched(), "A new board should not have all pairs matched.");
    }

    @Test
    @DisplayName("[TC-GB-04] Should contain the correct number and pairing of cards")
    void testBoardContainsCorrectCardPairs() {
        GameBoard board = new GameBoard(Difficulty.EASY); //16 total cards
        Map<String, Integer> cardIdCounts = new HashMap<>();

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                Card card = board.getCardAt(r, c);
                assertNotNull(card, "Card at (" + r + "," + c + ") should not be null.");
                cardIdCounts.put(card.getId(), cardIdCounts.getOrDefault(card.getId(), 0) + 1);
            }
        }

        assertEquals(8, cardIdCounts.size(), "There should be exactly 8 unique card pairs.");
        for (Map.Entry<String, Integer> entry : cardIdCounts.entrySet()) {
            assertEquals(2, entry.getValue(), "Each card ID should appear exactly twice. ID '" + entry.getKey() + "' did not.");
        }
    }

    @Test
    @DisplayName("[TC-GB-005] Should have randomized card placement across multiple initializations")
    void testCardPlacementIsRandomized() {
        final GameBoard baselineBoard = new GameBoard(Difficulty.HARD);
        final int boardSize = baselineBoard.getSize();

        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            GameBoard newBoard = new GameBoard(Difficulty.HARD);
            if (!areBoardsIdentical(baselineBoard, newBoard, boardSize)) {
                return;
            }
        }

        fail("Failed to generate a unique board layout after " + maxAttempts + " attempts. Shuffling may not be working.");
    }

    @Test
    @DisplayName("[TC-GB-06] getSize() should return the correct board dimension")
    void testGetSize() {
        GameBoard board = new GameBoard(Difficulty.MEDIUM);
        assertEquals(6, board.getSize());
    }

    @Test
    @DisplayName("[TC-GB-07 & 08] getCardAt() should retrieve a card from valid coordinates")
    void testGetCardAtValidCoordinates() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        assertNotNull(board.getCardAt(new Coordinate(0, 0)), "Should retrieve card from top-left corner.");
        assertNotNull(board.getCardAt(3, 3), "Should retrieve card from bottom-right corner.");
    }

    @Test
    @DisplayName("[TC-GB-09 & 10] getCardAt() should throw exception for out-of-bounds coordinates")
    void testGetCardAtOutOfBounds() {
        GameBoard board = new GameBoard(Difficulty.EASY); // valid indices 0-3
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> board.getCardAt(new Coordinate(-1, 0)), "Negative row should throw exception.");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> board.getCardAt(0, -1), "Negative column should throw exception.");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> board.getCardAt(4, 3), "Row index equal to size should throw exception.");
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> board.getCardAt(3, 4), "Column index equal to size should throw exception.");
    }

    @Test
    @DisplayName("[TC-GB-11] Should report no matches and all pairs remaining on a new board")
    void testInitialMatchState() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        assertFalse(board.areAllMatched());
        assertEquals(8, board.getRemainingPairs());
    }

    @Test
    @DisplayName("[TC-GB-12] Should update state correctly after one match is recorded")
    void testStateUpdateAfterOneMatch() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        board.recordMatch();
        assertFalse(board.areAllMatched());
        assertEquals(7, board.getRemainingPairs());
    }

    @Test
    @DisplayName("[TC-GB-13] areAllMatched() should be false just before the final match")
    void testAreAllMatchedBeforeCompletion() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        for (int i = 0; i < 7; i++) {
            board.recordMatch();
        }
        assertFalse(board.areAllMatched(), "Board should not be complete with one pair remaining.");
        assertEquals(1, board.getRemainingPairs(), "There should be one pair remaining.");
    }

    @Test
    @DisplayName("[TC-GB-14] areAllMatched() should be true when all pairs are matched")
    void testAreAllMatchedAtCompletion() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        for (int i = 0; i < 8; i++) {
            board.recordMatch();
        }
        assertTrue(board.areAllMatched(), "Board should be complete when all pairs are matched.");
    }

    @Test
    @DisplayName("[TC-GB-15] getRemainingPairs() should return zero at game completion")
    void testRemainingPairsAtCompletion() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        for (int i = 0; i < 8; i++) {
            board.recordMatch();
        }
        assertEquals(0, board.getRemainingPairs(), "There should be zero pairs remaining at game completion.");
    }

    @Test
    @DisplayName("[TC-GB-16] areAllMatched() should be false if recordMatch is called too many times")
    void testAreAllMatchedWhenOverMatched() {
        GameBoard board = new GameBoard(Difficulty.EASY);
        for (int i = 0; i < 9; i++) {
            board.recordMatch();
        }
        assertFalse(board.areAllMatched(), "areAllMatched should use strict equality and fail if match count exceeds total pairs.");
        assertEquals(-1, board.getRemainingPairs(), "Remaining pairs should become negative if over-matched.");
    }

    // Helper method to compare two boards for identical card placements
    private boolean areBoardsIdentical(GameBoard board1, GameBoard board2, int size) {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!board1.getCardAt(r, c).getId().equals(board2.getCardAt(r, c).getId())) {
                    return false;
                }
            }
        }
        return true;
    }
}
