package org.vv.exception;

public class InvalidCoordinateFormatException extends RuntimeException {
    public InvalidCoordinateFormatException() {}
    
    public InvalidCoordinateFormatException(String msg) {
        super(msg);
    }
}