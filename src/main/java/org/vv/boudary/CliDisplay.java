package org.vv.boudary;
import org.vv.entity.*;

import java.io.IOException;
import java.util.List;


import java.util.Comparator;

public class CliDisplay {

	public static void clearScreen() {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} else {
				System.out.print("\033[H\033[2J");
				System.out.flush();
			}
		} catch (IOException | InterruptedException e) {
			System.out.println(ErrorMessages.E013);
		}
	}

	public static void showHelp() {
		for (int i=0; i<2; i++) {
			System.out.println("\n");
		}
		System.out.println("=== MEMORY CARD GAME - HELP ===");
		System.out.println("Description: A command-line pair matching memory game.");
		System.out.println("\nHow to play:");
		System.out.println("  1. Start the game and choose the mode (1 or 2 players) and difficulty.");
		System.out.println("  2. Take turns selecting two cards using coordinates (e.g., 'A1', 'B3').");
		System.out.println("  3. If the cards match, they remain revealed and you score a point.");
		System.out.println("  4. If they don't match, they are flipped back face-down.");
		System.out.println("  5. The game ends when all pairs have been found.");
		System.out.println("\nCommand-line options:");
		System.out.println("  -h, --help           				Display this help message.");
		System.out.println("  -V, --version        				Display game version.");
		System.out.println("  -p, --players [1|2]  				Set number of players (default: 1).");
		System.out.println("  -d, --difficulty [easy|medium|hard] 		Set difficulty (default: easy).");
		System.out.println("  -n, --name \"Name\"    				Set a player's name (use twice for 2 players).");
		System.out.println("\nExamples:");
		System.out.println("  java -jar memogame.jar");
		System.out.println("  java -jar memogame.jar -p 2 -d medium -n \"Alice\" -n \"Bob\"");
		for (int i=0; i<2; i++) {
			System.out.println("\n");
		}
	}

	public static void showVersion() {
		System.out.println("Memory Card Game v1.0.0");
	}

	public static void displayBoard(GameState state) {
		clearScreen();
		GameBoard board = state.getBoard();
		List<Player> players = state.getPlayers();

		System.out.println("--- Score ---");
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			System.out.print(p.getName() + ": " + p.getScore() + " couples");
			if (i == state.getCurrentPlayerIndex()) {
				System.out.println(" <- CURRENT TURN");
			} else {
				System.out.println();
			}
		}

		if (players.size() == 1) {
			System.out.println("Total rounds: " + state.getTotalTurns());
		}

		System.out.println("Remaining couples: " + board.getRemainingPairs());
		System.out.println("-----------------");

		System.out.print("   ");
		for (int j = 0; j < board.getSize(); j++) {
			System.out.print(" " + (char) ('A' + j) + "  ");
		}
		System.out.println();

		for (int i = 0; i < board.getSize(); i++) {
			System.out.print(" " + (i + 1) + " ");
			for (int j = 0; j < board.getSize(); j++) {
				Card card = board.getCardAt(i, j);
				if (card.isRevealed()) {
					System.out.print("[" + card.getSymbol() + "] ");
				} else {
					System.out.print("[?] ");
				}
			}
			System.out.println();
		}
		System.out.println("-----------------");
	}

	public static void displayGameEnd(GameState state) {

		for (int i = 0; i < state.getBoard().getSize(); i++) {
			for (int j = 0; j < state.getBoard().getSize(); j++) {
				state.getBoard().getCardAt(i, j).setRevealed(true);
			}
		}
		displayBoard(state);

		System.out.println("================================");
		System.out.println("        GAME COMPLETED!         ");
		System.out.println("================================");

		System.out.println("Final scores:");
		List<Player> players = state.getPlayers();

		if (players.size() == 1) {
			System.out.println(
					players.get(0).getName() + " has completed the game in " + state.getTotalTurns() + " round.");
		} else {

			players.sort(Comparator.comparingInt(Player::getScore).reversed());

			for (Player p : players) {
				System.out.println(p.getName() + ": " + p.getScore() + " couples");
			}

			if (players.get(0).getScore() == players.get(1).getScore()) {
				System.out.println("It's a draw!");
			} else {
				System.out.println(players.get(0).getName() + " wins!");
			}
		}
	}

	public static void showError(String message) {
		System.out.println(message);
	}
}