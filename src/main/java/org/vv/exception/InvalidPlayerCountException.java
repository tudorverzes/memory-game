package org.vv.exception;

public class InvalidPlayerCountException extends RuntimeException {
    public InvalidPlayerCountException() {}
    
    public InvalidPlayerCountException(String msg) {
        super(msg);
    }
}