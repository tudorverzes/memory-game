package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.vv.entity.Difficulty;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DifficultyTest {
    @Test
    @DisplayName("[TC-D-01] Easy difficulty should have correct configuration")
    void testEasyConfiguration() {
        Difficulty easy = Difficulty.EASY;

        assertAll("Easy Difficulty Checks",
                () -> assertEquals(4, easy.getSize(), "Easy grid should be 4x4."),
                () -> assertEquals(8, easy.getUniquePairs(), "Easy mode should have 8 pairs."),
                () -> assertEquals(16, easy.getTotalCards(), "Easy mode should have 16 total cards.")
        );
    }

    @Test
    @DisplayName("[TC-D-02] Medium difficulty should have correct configuration")
    void testMediumConfiguration() {
        Difficulty medium = Difficulty.MEDIUM;

        assertAll("Medium Difficulty Checks",
                () -> assertEquals(6, medium.getSize(), "Medium grid should be 6x6."),
                () -> assertEquals(18, medium.getUniquePairs(), "Medium mode should have 18 pairs."),
                () -> assertEquals(36, medium.getTotalCards(), "Medium mode should have 36 total cards.")
        );
    }

    @Test
    @DisplayName("[TC-D-03] Hard difficulty should have correct configuration")
    void testHardConfiguration() {
        Difficulty hard = Difficulty.HARD;

        assertAll("Hard Difficulty Checks",
                () -> assertEquals(8, hard.getSize(), "Hard grid should be 8x8."),
                () -> assertEquals(32, hard.getUniquePairs(), "Hard mode should have 32 pairs."),
                () -> assertEquals(64, hard.getTotalCards(), "Hard mode should have 64 total cards.")
        );
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("[TC-D-04] getTotalCards() should equal size squared")
    void testGetTotalCardsCalculation(Difficulty difficulty) {
        int expectedTotal = difficulty.getSize() * difficulty.getSize();

        int actualTotal = difficulty.getTotalCards();

        assertEquals(expectedTotal, actualTotal,
                "Total cards for " + difficulty + " should be size squared (" + expectedTotal + ").");
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("[TC-D-005] Mathematical Invariant: Total cards must equal 2 * Unique Pairs")
    void testGameLogicInvariant(Difficulty difficulty) {
        int pairs = difficulty.getUniquePairs();
        int totalCards = difficulty.getTotalCards();

        assertEquals(totalCards, pairs * 2,
                "For difficulty " + difficulty + ", the total cards must be exactly double the unique pairs to ensure a solvable matching game.");
    }
}
