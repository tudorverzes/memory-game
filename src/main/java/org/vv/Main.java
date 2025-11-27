package org.vv;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.vv.entity.Difficulty;
import org.vv.entity.GameConfiguration;
import org.vv.entity.ErrorMessages;
import org.vv.boudary.CliDisplay;
import org.vv.boudary.InputHandler;
import org.vv.boudary.Game;

public class Main {
	
	public static void setupConsoleEncoding() throws UnsupportedEncodingException {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
    }
	
	public static void exit(int status) {
        System.exit(status);
    }

	public static void main(String[] args) {

		try {
            setupConsoleEncoding(); 
        } catch (UnsupportedEncodingException e) {
            System.out.println("ERRORE: UTF-8 non supportato");
        }

		if (args.length > 0) {
			if (args[0].equals("-h") || args[0].equals("--help")) {
				CliDisplay.showHelp();
				return;
			} else if (args[0].equals("-V") || args[0].equals("--version")) {
				CliDisplay.showVersion();
				return;
			}
		}
		
		try {
			if (args.length > 0) {
				runGameSession(args);
			} else {
				runMainMenu();
			}
			
		} catch (NoSuchElementException e) {
			System.out.println("\nGame ended.");
		}

		InputHandler.closeScanner();
		System.out.println("Thank you for playing!");
	}

	private static void runMainMenu() {
		boolean exit = false;
		while (!exit) {
			clearConsole();
			
			System.out.println("================================");
			System.out.println("MEMORY CARD GAME");
			System.out.println("================================");
			System.out.println("1. New Game");
			System.out.println("2. Help");
			System.out.println("3. Exit");
			System.out.print("Select option: ");

			String choice = InputHandler.getMenuOption();

			switch (choice.trim()) {
				case "1":
					runGameSession(new String[0]);
					break;
				case "2":
					clearConsole();
					CliDisplay.showHelp();
					System.out.println("\nPress Enter to return to menu...");
					InputHandler.getMenuOption();
					break;
				case "3":
					exit = true;
					break;
				default:
					System.out.println("Invalid option. Please select 1, 2, or 3.");
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					break;
			}
		}
	}

	private static void runGameSession(String[] initialArgs) {
		boolean playAgain = true;
		String[] currentArgs = initialArgs;

		while (playAgain) {
			clearConsole(); 
			GameConfiguration config = parseArgsOrSetup(currentArgs);

			Game game = new Game(config);
			//game.run();

			playAgain = InputHandler.getPlayAgain();
			currentArgs = new String[0]; 
		}
	}

	private static GameConfiguration parseArgsOrSetup(String[] args) {
		GameConfiguration response = null;
		if (args.length > 0) {
			response = parseCommandLineArgs(args);
		} else {
			response = interactiveSetup();
		}
		return response;}

	private static GameConfiguration interactiveSetup() {
		int numPlayers = InputHandler.getPlayerCount();
		List<String> playerNames = new ArrayList<>();
		for (int i = 0; i < numPlayers; i++) {
			String defaultName = "Player " + (i + 1);
			playerNames.add(InputHandler.getPlayerName(i + 1, defaultName));
		}
		Difficulty difficulty = InputHandler.getDifficulty();

		return new GameConfiguration(difficulty, numPlayers, playerNames);
	}

	private static GameConfiguration parseCommandLineArgs(String[] args) {

		Difficulty difficulty = Difficulty.EASY;
		int numPlayers = 1;
		List<String> playerNames = new ArrayList<>();

		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-p") || args[i].equals("--players")) {
					if (i + 1 < args.length) {
						int p = Integer.parseInt(args[++i]);
						if (p == 1 || p == 2) {
							numPlayers = p;
						} else {
							CliDisplay.showError(ErrorMessages.E009);
							exit(1);
						}
					}
				} else if (args[i].equals("-d") || args[i].equals("--difficulty")) {
					if (i + 1 < args.length) {
						String diffStr = args[++i].toUpperCase();
						try {
							difficulty = Difficulty.valueOf(diffStr);
						} catch (IllegalArgumentException e) {
							CliDisplay.showError(ErrorMessages.E008);
							exit(1);
						}
					}
				} else if (args[i].equals("-n") || args[i].equals("--name")) {
					if (i + 1 < args.length) {
						String name = args[++i];
						if (name.length() > 20) {
							CliDisplay.showError("Name too long (max 20 characters). It will be truncated.");
							playerNames.add(name.substring(0, 20));
						}
						else {
							playerNames.add(name);
						}
					}
				} else {
					CliDisplay.showError(ErrorMessages.E010 + ": " + args[i]);
					exit(1);
				}
			}

			if (playerNames.size() > numPlayers) {
				CliDisplay.showError(ErrorMessages.E007);
				exit(1);
			}

		} catch (NumberFormatException e) {
			CliDisplay.showError(ErrorMessages.E009);
			exit(1);
		} catch (Exception e) {
			CliDisplay.showError("Error generated in argument analisys: " + e.getMessage());
			exit(1);
		}

		return new GameConfiguration(difficulty, numPlayers, playerNames);
	}
	
	public static void clearConsole() {
		try {
			final String os = System.getProperty("os.name");

			if (os.contains("Windows")) {
				// For Windows
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} else {
				// For Linux, macOS and other Unix-like systems
				System.out.print("\033[H\033[2J");
				System.out.flush();
			}
		} catch (final IOException | InterruptedException e) {
			System.err.println("Errore durante la pulizia della console: " + e.getMessage());
		}
	}
}
