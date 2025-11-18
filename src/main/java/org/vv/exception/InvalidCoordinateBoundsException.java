package org.vv.exception;

public class InvalidCoordinateBoundsException extends Exception {
    public InvalidCoordinateBoundsException() {}
    
    public InvalidCoordinateBoundsException(String msg) {
        super(msg);
    }
}