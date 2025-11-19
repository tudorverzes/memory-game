package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vv.entity.Player;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    @Test
    @DisplayName("[TC-P-001] Should initialize with correct name and zero score")
    void testPlayerInitialization() {
        String playerName = "Alice";

        Player player = new Player(playerName);

        assertAll("Player Initialization",
                () -> assertEquals(playerName, player.getName(), "Player name should be set correctly by the constructor."),
                () -> assertEquals(0, player.getScore(), "A new player's score should always be initialized to 0.")
        );
    }

    @Test
    @DisplayName("[TC-P-002] incrementScore() should increase score by one")
    void testIncrementScoreOnce() {
        Player player = new Player("Bob");
        assertEquals(0, player.getScore(), "Precondition: Score should be 0.");

        player.incrementScore();

        assertEquals(1, player.getScore(), "Score should be 1 after a single increment.");
    }

    @Test
    @DisplayName("[TC-P-003] incrementScore() should accumulate score correctly")
    void testIncrementScoreMultipleTimes() {
        Player player = new Player("Charlie");

        player.incrementScore(); // Score becomes 1
        player.incrementScore(); // Score becomes 2
        player.incrementScore(); // Score becomes 3

        assertEquals(3, player.getScore(), "Score should be 3 after three increments.");
    }

    @Test
    @DisplayName("[TC-P-004] Player name should be immutable")
    void testPlayerNameIsImmutable() {
        String initialName = "David";
        Player player = new Player(initialName);

        player.incrementScore();

        assertEquals(initialName, player.getName(), "Player name should not change even when other state (like score) changes.");
    }

    @Test
    @DisplayName("[TC-P-005] Should handle a null name in the constructor")
    void testConstructorWithNullName() {
        Player player = new Player(null);

        assertNull(player.getName(), "Name should be null if null is passed to the constructor.");
        assertEquals(0, player.getScore(), "Score should still be initialized to 0 even with a null name.");
    }

    @Test
    @DisplayName("[TC-P-006] Should handle an empty string name in the constructor")
    void testConstructorWithEmptyStringName() {
        Player player = new Player("");

        assertEquals("", player.getName(), "Name should be an empty string if passed to the constructor.");
        assertEquals(0, player.getScore(), "Score should be initialized to 0.");
    }
}
