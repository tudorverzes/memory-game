package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vv.entity.Coordinate;
import org.vv.exception.InvalidCoordinateBoundsException;
import org.vv.exception.InvalidCoordinateFormatException;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {
    @Test
    @DisplayName("[TC-CO-001] Constructor should set row and col correctly")
    void testConstructorAndGetters() {
        Coordinate coord = new Coordinate(1, 2);
        assertAll("Constructor and Getters",
                () -> assertEquals(1, coord.getRow(), "getRow() should return the value set in the constructor."),
                () -> assertEquals(2, coord.getCol(), "getCol() should return the value set in the constructor.")
        );
    }

    @Test
    @DisplayName("[TC-CO-002 & 005] fromString should parse valid standard and boundary coordinates")
    void testFromStringValidCoordinates() {
        assertAll("Valid Coordinate Parsing",
                () -> {
                    Coordinate c = Coordinate.fromString("A1", 4);
                    assertEquals(0, c.getRow());
                    assertEquals(0, c.getCol());
                },
                () -> {
                    Coordinate c = Coordinate.fromString("D4", 4);
                    assertEquals(3, c.getRow());
                    assertEquals(3, c.getCol());
                }
        );
    }

    @Test
    @DisplayName("[TC-CO-003] fromString should handle lowercase input")
    void testFromStringLowercase() {
        Coordinate coord = assertDoesNotThrow(() -> Coordinate.fromString("b3", 8));
        assertEquals(2, coord.getRow());
        assertEquals(1, coord.getCol());
    }

    @Test
    @DisplayName("[TC-CO-004] fromString should handle leading/trailing whitespace")
    void testFromStringWithWhitespace() {
        Coordinate coord = assertDoesNotThrow(() -> Coordinate.fromString("  c5  ", 8));
        assertEquals(4, coord.getRow());
        assertEquals(2, coord.getCol());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "A123", ""})
    @DisplayName("[TC-CO-006] fromString should throw format exception for invalid length")
    void testFromStringInvalidLength(String input) {
        assertThrows(InvalidCoordinateFormatException.class, () -> Coordinate.fromString(input, 4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AX", "C-"})
    @DisplayName("[TC-CO-007] fromString should throw format exception for non-numeric row")
    void testFromStringInvalidFormat(String input) {
        assertThrows(InvalidCoordinateFormatException.class, () -> Coordinate.fromString(input, 8));
    }

    @ParameterizedTest
    @ValueSource(strings = {"E1", "A5", "H8"}) // For a 4x4 grid (A-D, 1-4)
    @DisplayName("[TC-CO-008 & 009] fromString should throw bounds exception for out-of-bounds input")
    void testFromStringOutOfBounds(String input) {
        assertThrows(InvalidCoordinateBoundsException.class, () -> Coordinate.fromString(input, 4));
    }

    @Test
    @DisplayName("[TC-CO-010] fromString should throw bounds exception for row zero")
    void testFromStringRowZero() {
        assertThrows(InvalidCoordinateBoundsException.class, () -> Coordinate.fromString("A0", 4));
    }

    @Test
    @DisplayName("[TC-CO-011] equals() and hashCode() should hold contract for equal objects")
    void testEqualsAndHashCodeForEqualObjects() {
        Coordinate coord1 = new Coordinate(3, 4);
        Coordinate coord2 = new Coordinate(3, 4);

        assertEquals(coord1, coord2, "Two coordinates with the same row and col should be equal.");
        assertEquals(coord1.hashCode(), coord2.hashCode(), "Hash codes of equal objects must be equal.");
    }

    @Test
    @DisplayName("[TC-CO-012] equals() should return false for unequal objects")
    void testEqualsForUnequalObjects() {
        Coordinate coord1 = new Coordinate(3, 4);
        Coordinate coord2 = new Coordinate(4, 3);
        Coordinate coord3 = new Coordinate(3, 5);
        Coordinate coord4 = new Coordinate(2, 4);

        assertNotEquals(coord1, coord2);
        assertNotEquals(coord1, coord3);
        assertNotEquals(coord1, coord4);
    }

    @Test
    @DisplayName("[TC-CO-015] equals() should return true for the same object instance (reflexivity)")
    void testEqualsSameInstance() {
        Coordinate coord = new Coordinate(5, 5);
        assertEquals(coord, coord, "An object must be equal to itself.");
    }


    @Test
    @DisplayName("[TC-CO-013] equals() should be robust against null and different types")
    void testEqualsRobustness() {
        Coordinate coord = new Coordinate(1, 1);
        Object otherObject = new Object();

        assertNotEquals(coord, null, "Equals should return false for a null comparison.");
        assertNotEquals(coord, otherObject, "Equals should return false for comparison with a different class.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"&3", "%1"})
    @DisplayName("[TC-CO-014] fromString should throw bounds exception for non-alphabetic column")
    void testFromStringInvalidColumnCharacter(String input) {
        assertThrows(InvalidCoordinateBoundsException.class, () -> Coordinate.fromString(input, 8));
    }
}
