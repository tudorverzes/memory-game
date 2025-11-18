package org.vv.entity;

public class Card {

    private final String id;
    private final String symbol;
    private boolean isRevealed;

    public Card(String id, String symbol) {
        this.id = id;
        this.symbol = symbol;
        this.isRevealed = false;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void setRevealed(boolean revealed) {
        isRevealed = revealed;
    }
}