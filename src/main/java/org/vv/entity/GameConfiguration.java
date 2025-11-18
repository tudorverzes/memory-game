package org.vv.entity;
import java.util.List;


public class GameConfiguration {

    private final Difficulty difficulty;
    private final int numPlayers;
    private final List<String> playerNames;

    public GameConfiguration(Difficulty difficulty, int numPlayers, List<String> playerNames) {
        this.difficulty = difficulty;
        this.numPlayers = numPlayers;
        this.playerNames = playerNames;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }
}