package org.vv.entity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CardSymbols {
    private static final List<String> SYMBOLS = Arrays.asList(
            "1", "2", "3", "4",
            "5", "6", "7", "8",
            "@", "#", "$", "%",
            "&", "+", "=", "!",
            "A", "B", "C", "D",
            "E", "F", "G", "H",
            "K", "L", "M", "N",
            "P", "Q", "R", "T"
    );

    public static List<String> getSymbols(int numPairs) {
        if (numPairs > SYMBOLS.size()) {
            throw new IllegalArgumentException("Not enough symbols for " 
                + numPairs + " couples.");
        }


        List<String> shuffledSymbols = new ArrayList<>(SYMBOLS);
        Collections.shuffle(shuffledSymbols);
        
        return shuffledSymbols.subList(0, numPairs);
    }
}