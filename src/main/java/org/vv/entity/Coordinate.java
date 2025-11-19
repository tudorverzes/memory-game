package org.vv.entity;
import org.vv.exception.InvalidCoordinateBoundsException;
import org.vv.exception.InvalidCoordinateFormatException;

import java.util.Objects;

public class Coordinate {

    private final int row;
    private final int col;

    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }


    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


    public static Coordinate fromString(String input, int gridSize) throws IllegalArgumentException, InvalidCoordinateBoundsException {
        input = input.trim().toUpperCase();
        
        if (input.length() < 2 || input.length() > 3) {
            throw new InvalidCoordinateFormatException(ErrorMessages.E004);
        }

        char colChar = input.charAt(0);
        String rowStr = input.substring(1);

        int col = colChar - 'A';
        int row;

        try {
            row = Integer.parseInt(rowStr) - 1;
        } catch (NumberFormatException e) {
            throw new InvalidCoordinateFormatException(ErrorMessages.E004);
        }


        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
            throw new InvalidCoordinateBoundsException(ErrorMessages.E001);
        }

        return new Coordinate(row, col);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}