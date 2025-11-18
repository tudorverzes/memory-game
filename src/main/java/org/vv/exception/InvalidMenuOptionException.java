package org.vv.exception;

public class InvalidMenuOptionException extends RuntimeException {
    public InvalidMenuOptionException() {}
    
    public InvalidMenuOptionException(String msg) {
        super(msg);
    }
}