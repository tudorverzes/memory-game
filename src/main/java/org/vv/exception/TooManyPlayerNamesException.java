package org.vv.exception;

public class TooManyPlayerNamesException extends RuntimeException {
    public TooManyPlayerNamesException() {}
    
    public TooManyPlayerNamesException(String msg) {
        super(msg);
    }
}