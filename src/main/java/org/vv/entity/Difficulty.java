package org.vv.entity;

public enum Difficulty {
    EASY(4, 8),
    MEDIUM(6, 18),
    HARD(8, 32);

    private final int size; 
    private final int uniquePairs;

    Difficulty(int size, int uniquePairs) {
        this.size = size;
        this.uniquePairs = uniquePairs;
    }
    public int getSize() {
        return size;
    }

    public int getUniquePairs() {
        return uniquePairs;
    }

    public int getTotalCards() {
        return size * size;
    }
}