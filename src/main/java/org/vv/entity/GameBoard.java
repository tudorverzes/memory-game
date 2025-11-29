package org.vv.entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GameBoard {

    private final Card[][] grid;
    private final int size;
    private int matchedPairsCount;
    private final int totalPairs;

    public GameBoard(Difficulty difficulty) {
        this.size = difficulty.getSize();
        this.grid = new Card[size][size];
        this.matchedPairsCount = 0;
        this.totalPairs = difficulty.getUniquePairs();
        initializeBoard();
    }


    private void initializeBoard() {
        List<String> symbols = CardSymbols.getSymbols(totalPairs);
        List<Card> cardsToPlace = new ArrayList<>();

        for (int i = 0; i < totalPairs; i++) {
            String symbol = symbols.get(i);
            String id = "pair-" + i;
            cardsToPlace.add(new Card(id, symbol));
            cardsToPlace.add(new Card(id, symbol));
        }

        Collections.shuffle(cardsToPlace);


        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = cardsToPlace.get(k++);
            }
        }
    }

    public int getSize() {
        return size;
    }

    public Card getCardAt(Coordinate coord) {
        return grid[coord.getRow()][coord.getCol()];
    }

    public Card getCardAt(int row, int col) {
        return grid[row][col];
    }

    public void recordMatch() {
        this.matchedPairsCount++;
    }

    public boolean areAllMatched() {
        return matchedPairsCount == totalPairs;
    }
    
    public int getRemainingPairs() {
        return totalPairs - matchedPairsCount;
    }
}