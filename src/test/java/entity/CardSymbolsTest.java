package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.vv.entity.CardSymbols;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CardSymbolsTest {
    private static final int MAX_SYMBOLS = 32;

    @Test
    @DisplayName("[TC-CS-001] Should return a list of the requested size for a standard request")
    void testGetSymbolsBasicRequest() {
        int requestedSize = 8;

        List<String> symbols = CardSymbols.getSymbols(requestedSize);

        assertNotNull(symbols);
        assertEquals(requestedSize, symbols.size(), "The returned list should have the exact number of symbols requested.");
    }

    @Test
    @DisplayName("[TC-CS-002] Should return a full list when requesting the maximum number of symbols")
    void testGetSymbolsMaximumRequest() {
        int requestedSize = MAX_SYMBOLS;

        List<String> symbols = CardSymbols.getSymbols(requestedSize);

        assertNotNull(symbols);
        assertEquals(requestedSize, symbols.size(), "The returned list should contain all available symbols.");
    }

    @Test
    @DisplayName("[TC-CS-003] Should return an empty list when requesting zero symbols")
    void testGetSymbolsForZeroPairs() {
        int requestedSize = 0;

        List<String> symbols = CardSymbols.getSymbols(requestedSize);

        assertNotNull(symbols, "The method should return a non-null list even for zero pairs.");
        assertTrue(symbols.isEmpty(), "The returned list should be empty when zero symbols are requested.");
    }

    @Test
    @DisplayName("[TC-CS-004] Should throw IllegalArgumentException if more symbols are requested than available")
    void testGetSymbolsThrowsExceptionForTooManyPairs() {
        int requestedSize = MAX_SYMBOLS + 1;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CardSymbols.getSymbols(requestedSize);
        }, "An IllegalArgumentException should be thrown when requesting too many symbols.");

        assertTrue(exception.getMessage().contains("Not enough symbols"), "The exception message should be informative.");
    }

    @Test
    @DisplayName("[TC-CS-005] Should throw IllegalArgumentException if a negative number of symbols is requested")
    void testGetSymbolsThrowsExceptionForNegativePairs() {
        int requestedSize = -1;

        assertThrows(IllegalArgumentException.class, () -> {
            CardSymbols.getSymbols(requestedSize);
        }, "An IllegalArgumentException should be thrown for a negative request.");
    }

    @Test
    @DisplayName("[TC-CS-006] Should return a list of unique symbols")
    void testGetSymbolsReturnsUniqueSymbols() {
        int requestedSize = 18;

        List<String> symbols = CardSymbols.getSymbols(requestedSize);
        Set<String> uniqueSymbols = new HashSet<>(symbols);

        assertEquals(symbols.size(), uniqueSymbols.size(), "All symbols in the returned list should be unique.");
    }

    @RepeatedTest(5) // Increase confidence in randomness
    @DisplayName("[TC-CS-007] Should return a different, randomized list on each call")
    void testGetSymbolsReturnsRandomizedLists() {
        int requestedSize = 16;

        List<String> list1 = CardSymbols.getSymbols(requestedSize);
        List<String> list2 = CardSymbols.getSymbols(requestedSize);

        assertEquals(requestedSize, list1.size());
        assertEquals(requestedSize, list2.size());
        assertNotEquals(list1, list2, "Two consecutive calls should produce differently ordered lists due to shuffling. This test has a minuscule probability of a random collision.");
    }
}
