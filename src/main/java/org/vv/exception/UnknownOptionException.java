package org.vv.exception;

public class UnknownOptionException extends RuntimeException {
    public UnknownOptionException() {}
    
    public UnknownOptionException(String msg) {
        super(msg);
    }
}