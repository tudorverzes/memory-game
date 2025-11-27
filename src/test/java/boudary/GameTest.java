package boudary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.vv.boudary.Game;
import org.vv.entity.GameConfiguration;
import org.vv.entity.Difficulty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        
        System.setIn(originalIn);
        System.setErr(originalErr);
    }

    @Test
    void testPlayTurn_InterruptionOnNoMatch() throws InterruptedException {
        
        String simulatedInput = "A1" + System.lineSeparator() + 
                                "B1" + System.lineSeparator() + 
                                "exit" + System.lineSeparator();
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        
        List<String> players = new ArrayList<>();
        players.add("Tester");
        
        GameConfiguration config = new GameConfiguration(Difficulty.EASY, 1, players);
        
        Game game = new Game(config); 

        
        Thread gameThread = new Thread(() -> {
            try {
                game.run(); 
            } catch (Exception e) {
                
            }
        });

        gameThread.start();

        Thread.sleep(1500);
        
        gameThread.interrupt();
        
        gameThread.join(2000);

        
        String errorOutput = errContent.toString();
        
        
        boolean matchFound = outContent.toString().contains("Match found");
        if (matchFound) {
            System.out.println("ATTENZIONE: Il test è stato saltato perché le carte estratte a caso erano uguali!");
            return; 
        }

        assertTrue(errorOutput.contains("Pause interrupted"), 
            "System.err catturato: [" + errorOutput + "] -> Manca 'Pause interrupted'. " +
            "Controlla che l'input A1/B1 sia accettato e che le carte non fossero uguali.");
    }
}
