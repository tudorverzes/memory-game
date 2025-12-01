package org.vv.boudary;
import org.vv.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Game {

	private final GameState state;

	public Game(GameConfiguration config) {

		List<Player> players = new ArrayList<>();
		for (int i = 0; i < config.getNumPlayers(); i++) {
			String name;
			if (i < config.getPlayerNames().size()) {
				name = config.getPlayerNames().get(i);
			} else {
				name = "Player " + (i + 1);
			}
			players.add(new Player(name));
		}

		GameBoard board = new GameBoard(config.getDifficulty());

		this.state = new GameState(board, players, config.getDifficulty());
		System.out.println("Game starts...Good luck!");
	}

	public void run() {
        while (!state.isComplete()) {
			playTurn();
			checkGameCompletion();
		}
		endGame();
	}

	private void playTurn() {
		CliDisplay.displayBoard(state);
		Player currentPlayer = state.getCurrentPlayer();

		if (state.getPlayers().size() == 1) {
			state.incrementTotalTurns();
		}

		Coordinate c1 = InputHandler.getCoordinateInput(
				currentPlayer.getName() + " selects first card(es. A1): ", state.getBoard().getSize(),
				state.getBoard(), null);
		Card card1 = state.getBoard().getCardAt(c1);
		card1.setRevealed(true);
		CliDisplay.displayBoard(state);

		Coordinate c2 = InputHandler.getCoordinateInput(currentPlayer.getName() + " selects second card: ",
				state.getBoard().getSize(), state.getBoard(), c1);
		Card card2 = state.getBoard().getCardAt(c2);
		card2.setRevealed(true);
		CliDisplay.displayBoard(state);

		if (card1.getId().equals(card2.getId())) {

			System.out.println("Match found! Great job, " + currentPlayer.getName() + "!");
			currentPlayer.incrementScore();
			state.getBoard().recordMatch();
		} else {

			System.out.println("No match found. Retry.");
			try {
				TimeUnit.SECONDS.sleep(5);
			}  catch (InterruptedException e) {
                System.err.println("Pause interrupted");
                Thread.currentThread().interrupt();
                return;
            }
			card1.setRevealed(false);
			card2.setRevealed(false);
			state.nextTurn();
		}

	}

    private void checkGameCompletion() {
        if (!state.isComplete() && state.getBoard().areAllMatched()) {
            state.setComplete(true);
        }
    }

	private void endGame() {
		CliDisplay.displayGameEnd(state);
	}
}