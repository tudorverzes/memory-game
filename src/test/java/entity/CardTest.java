package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vv.entity.Card;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    @Test
    @DisplayName("[TC-C-01] Should initialize with correct ID, symbol, and not revealed")
    void testCardInitialization() {
        String expectedId = "pair-1";
        String expectedSymbol = "A";

        Card card = new Card(expectedId, expectedSymbol);

        assertEquals(expectedId, card.getId(), "Card ID should be set correctly by the constructor.");
        assertEquals(expectedSymbol, card.getSymbol(), "Card symbol should be set correctly by the constructor.");
        assertFalse(card.isRevealed(), "A new card should always be initialized with isRevealed = false.");
    }

    @Test
    @DisplayName("[TC-C-02] Should transition from not revealed to revealed")
    void testSetRevealedToTrue() {
        Card card = new Card("pair-2", "A");
        assertFalse(card.isRevealed(), "Precondition: Card should start as not revealed.");

        card.setRevealed(true);

        assertTrue(card.isRevealed(), "Card should be revealed after setRevealed(true) is called.");
    }

    @Test
    @DisplayName("[TC-C-03] Should transition from revealed back to not revealed")
    void testSetRevealedToFalseAfterBeingTrue() {
        Card card = new Card("pair-3", "B");
        card.setRevealed(true);
        assertTrue(card.isRevealed(), "Precondition: Card should be in a revealed state.");

        card.setRevealed(false);

        assertFalse(card.isRevealed(), "Card should be not revealed after setRevealed(false) is called.");
    }

    @Test
    @DisplayName("[TC-C-04] ID and Symbol should be immutable")
    void testIdAndSymbolAreImmutable() {
        String initialId = "id_original";
        String initialSymbol = "sym_original";
        Card card = new Card(initialId, initialSymbol);

        card.setRevealed(true);

        assertEquals(initialId, card.getId(), "Card ID should not change after other mutations.");
        assertEquals(initialSymbol, card.getSymbol(), "Card symbol should not change after other mutations.");
    }

    @Test
    @DisplayName("[TC-C-05] setRevealed should be idempotent")
    void testSetRevealedIsIdempotent() {
        Card card = new Card("pair-4", "C");

        card.setRevealed(true);
        assertTrue(card.isRevealed(), "Card is set to true.");
        card.setRevealed(true); // Call again with the same value
        assertTrue(card.isRevealed(), "Calling setRevealed(true) on a revealed card should keep it revealed.");

        card.setRevealed(false);
        assertFalse(card.isRevealed(), "Card is set to false.");
        card.setRevealed(false); // Call again with the same value
        assertFalse(card.isRevealed(), "Calling setRevealed(false) on a hidden card should keep it hidden.");
    }

    @Test
    @DisplayName("[TC-C-06] Should handle null values in constructor")
    void testConstructorWithNullValues() {
        Card card = new Card(null, null);

        assertNull(card.getId(), "ID should be null if null is passed to the constructor.");
        assertNull(card.getSymbol(), "Symbol should be null if null is passed to the constructor.");
        assertFalse(card.isRevealed(), "isRevealed should still be false even with null constructor arguments.");
    }

    @Test
    @DisplayName("[TC-C-07] Should handle empty strings in constructor")
    void testConstructorWithEmptyStrings() {
        Card card = new Card("", "");

        assertEquals("", card.getId(), "ID should be an empty string if passed to the constructor.");
        assertEquals("", card.getSymbol(), "Symbol should be an empty string if passed to the constructor.");
    }
}
