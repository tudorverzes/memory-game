package org.vv.exception;

public class SameCardSelectionException extends RuntimeException {
    public SameCardSelectionException() {}
    
    public SameCardSelectionException(String msg) {
        super(msg);
    }
}