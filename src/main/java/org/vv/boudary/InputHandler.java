package org.vv.boudary;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.vv.entity.Coordinate;
import org.vv.entity.Difficulty;
import org.vv.entity.GameBoard;
import org.vv.entity.ErrorMessages;
import org.vv.exception.CardAlreadyMatchedException;
import org.vv.exception.InvalidCoordinateBoundsException;
import org.vv.exception.InvalidCoordinateFormatException;
import org.vv.exception.MaxAttemptsExceededException;
import org.vv.exception.SameCardSelectionException;

public class InputHandler {

	private static Scanner scanner = new Scanner(System.in);
	private static final int MAX_ATTEMPTS = 5;

	public static String getPlayerName(int playerNum, String defaultName) {
		System.out.print("Insert player's name " + playerNum + " (default: " + defaultName + "): ");
		String name = scanner.nextLine().trim();

		if (name.isEmpty()) {
			return defaultName;
		}

		if (name.length() > 20) {
			CliDisplay.showError("Name too long (max 20 characters). It will be truncated.");
			name = name.substring(0, 20);
		}

		return name;
	}

	public static int getPlayerCount() {
		int attempts = 0;
		while (attempts < MAX_ATTEMPTS) {
			System.out.print("Players number (1-2): ");
			try {
				String input = scanner.nextLine().trim();
				int count = Integer.parseInt(input);
				if (count == 1 || count == 2) {
					return count;
				} else {
					CliDisplay.showError(ErrorMessages.E009);
				}
			} catch (NumberFormatException e) {
				CliDisplay.showError(ErrorMessages.E005);
			}
			attempts++;
		}
		throw new MaxAttemptsExceededException(ErrorMessages.E015);
	}

	public static Difficulty getDifficulty() {
		int attempts = 0;
		while (attempts < MAX_ATTEMPTS) {
			System.out.println("Select difficulty:");
			System.out.println("  1. Easy (4x4)");
			System.out.println("  2. Medium (6x6)");
			System.out.println("  3. Hard (8x8)");
			System.out.print("Type 1, 2 or 3: ");

			String input = scanner.nextLine().trim().toLowerCase();

			if (input.equals("1")) {
				return Difficulty.EASY;
			} else if (input.equals("2")) {
				return Difficulty.MEDIUM;
			} else if (input.equals("3")) {
				return Difficulty.HARD;
			} else {
				CliDisplay.showError(ErrorMessages.E005);
			}
			attempts++;
		}
		throw new MaxAttemptsExceededException(ErrorMessages.E015);
	}

	public static Coordinate getCoordinateInput(String prompt, int gridSize, GameBoard board, Coordinate firstCard) {
	    
	    while (true) {
	        System.out.print(prompt);
	        String input = scanner.nextLine();

	        try {
	            Coordinate coord = Coordinate.fromString(input, gridSize);

	            if (board.getCardAt(coord).isRevealed()) {
	                throw new CardAlreadyMatchedException(ErrorMessages.E003);
	            }

	            if (firstCard != null && coord.equals(firstCard)) {
	                throw new SameCardSelectionException(ErrorMessages.E002);
	            }

	            return coord;

	        } catch (IllegalArgumentException e) {
	            CliDisplay.showError(e.getMessage());
	        } catch (InvalidCoordinateFormatException e) {
	            CliDisplay.showError(e.getMessage());
	        } catch (InvalidCoordinateBoundsException e) {
	            CliDisplay.showError(e.getMessage());
	        } catch (CardAlreadyMatchedException e) { 
	            CliDisplay.showError(e.getMessage());
	        } catch (SameCardSelectionException e) { 
	            CliDisplay.showError(e.getMessage());
	        }
	    }
	}

	public static boolean getPlayAgain() {
		while (true) {
			System.out.print("Do you want to play again?? (y/n): ");
			String input = scanner.nextLine().trim().toLowerCase();

			if (input.equals("y") || input.equals("yes")) {
				return true;
			} else if (input.equals("n") || input.equals("no")) {
				return false;
			} else {
				CliDisplay.showError(ErrorMessages.E005);
			}
		}
	}

	public static void closeScanner() {
		scanner.close();
	}
	
	public static String getMenuOption() {
		try {
			return scanner.nextLine();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("Input stream closed.");
		}
	}
}