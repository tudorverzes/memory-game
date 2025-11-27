package boudary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import org.vv.boudary.CliDisplay;
import org.vv.entity.ErrorMessages;

class CliDisplayTest {
	
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        // Redirigiamo System.err per catturare i messaggi di errore senza sporcare la console
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void tearDown() {

        System.setErr(originalErr);
    }


    @Test
	void testClearScreen_InterruptedException_CheckOutput() throws IOException {
	    // 1. SETUP PER CATTURARE LA CONSOLE
	    // Salviamo lo stream originale per ripristinarlo dopo
	    PrintStream originalOut = System.out;
	    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outContent));
	    
	    // Salviamo l'OS originale
	    String originalOs = System.getProperty("os.name");

	    try {
	        // 2. FORZA WINDOWS
	        System.setProperty("os.name", "Windows 10");

	        // 3. MOCKING DEL PROCESSO CHE FALLISCE
	        Process mockProcess = Mockito.mock(Process.class);
	        // Quando gli diciamo di aspettare (waitFor), lui lancia l'InterruptedException!
	        Mockito.when(mockProcess.waitFor()).thenThrow(new InterruptedException("Interrotto!"));

	        // 4. MOCKING DEL PROCESS BUILDER
	        try (MockedConstruction<ProcessBuilder> mockedPb = Mockito.mockConstruction(ProcessBuilder.class,
	                (mock, context) -> {
	                    Mockito.when(mock.inheritIO()).thenReturn(mock);
	                    Mockito.when(mock.start()).thenReturn(mockProcess);
	                })) {

	            // 5. ESECUZIONE
	            // Se il metodo è in CliDisplay usa CliDisplay.clearScreen(), se è in Main usa Main.clearScreen()
	            CliDisplay.clearScreen(); 
	        }

	        // 6. VERIFICA DELL'OUTPUT
	        // Controlliamo che nello stream catturato ci sia il messaggio di errore E013
	        // Usiamo trim() per ignorare eventuali spazi o a capo finali
	        assertTrue(outContent.toString().contains(ErrorMessages.E013), 
	                   "Dovrebbe stampare il messaggio di errore E013 quando il processo viene interrotto");

	    } catch (Exception e) {
	        fail("Eccezione non prevista nel test: " + e.getMessage());
	    } finally {
	        // 7. RIPRISTINO TOTALE (TEARDOWN)
	        // È fondamentale rimettere a posto System.out e l'OS, altrimenti rompiamo gli altri test
	        System.setOut(originalOut);
	        System.setProperty("os.name", originalOs);
	    }
	}
	
	@Test
    void testClearScreen_Linux() {
        // 1. SALVATAGGIO DELLO STATO ORIGINALE
        // È fondamentale salvare il nome del vero OS e lo stream di output
        // per ripristinarli alla fine, altrimenti spacchi gli altri test.
        String originalOs = System.getProperty("os.name");
        PrintStream originalOut = System.out;
        
        // Prepariamo la "trappola" per catturare quello che viene stampato
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // 2. SETUP: FINGIAMO DI ESSERE LINUX
            // Impostiamo una stringa che NON contiene "win"
            System.setProperty("os.name", "Linux");

            // 3. ESECUZIONE
            // Il codice entrerà nel ramo 'else' perché os.contains("win") è falso
            CliDisplay.clearScreen();

            // 4. VERIFICA
            // Ci aspettiamo che venga stampata la sequenza ANSI: \033[H\033[2J
            // Nota: \033 è il carattere ESC (Escape).
            assertEquals("\033[H\033[2J", outContent.toString());

        } finally {
            // 5. RIPRISTINO (TEARDOWN)
            // Rimettiamo le cose a posto anche se il test fallisce
            System.setProperty("os.name", originalOs);
            System.setOut(originalOut);
        }
    }

}
