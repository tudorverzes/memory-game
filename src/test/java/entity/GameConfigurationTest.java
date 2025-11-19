package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vv.entity.Difficulty;
import org.vv.entity.GameConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameConfigurationTest {

    @Test
    @DisplayName("TC-CONF-01: Constructor stores values correctly for 2 players")
    void testConstructorStoresValues() {
        Difficulty difficulty = Difficulty.EASY;
        int numPlayers = 2;
        List<String> players = List.of("Alice", "Bob");

        GameConfiguration config = new GameConfiguration(difficulty, numPlayers, players);

        assertEquals(difficulty, config.getDifficulty());
        assertEquals(numPlayers, config.getNumPlayers());
        assertEquals(players, config.getPlayerNames());
    }

    @Test
    @DisplayName("TC-CONF-02: Constructor stores values correctly for 1 player")
    void testSinglePlayerConfiguration() {
        Difficulty difficulty = Difficulty.MEDIUM;
        int numPlayers = 1;
        List<String> players = List.of("AI");

        GameConfiguration config = new GameConfiguration(difficulty, numPlayers, players);

        assertEquals(difficulty, config.getDifficulty());
        assertEquals(numPlayers, config.getNumPlayers());
        assertEquals(players, config.getPlayerNames());
    }
}
