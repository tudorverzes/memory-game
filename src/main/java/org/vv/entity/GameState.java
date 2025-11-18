package org.vv.entity;
import java.util.List;


public class GameState {

    private final GameBoard board;
    private final List<Player> players;
    private final Difficulty difficulty;
    private int currentPlayerIndex;
    private int totalTurns;
    private boolean isComplete;

    public GameState(GameBoard board, List<Player> players, Difficulty difficulty) {
        this.board = board;
        this.players = players;
        this.difficulty = difficulty;
        this.currentPlayerIndex = 0;
        this.totalTurns = 0;
        this.isComplete = false;
    }

    public GameBoard getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public int getTotalTurns() { return totalTurns; }
    public boolean isComplete() { return isComplete; }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public void incrementTotalTurns() {
        this.totalTurns++;
    }

    public void nextTurn() {
        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();
    }
}