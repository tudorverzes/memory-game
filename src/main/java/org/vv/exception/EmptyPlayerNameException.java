package org.vv.exception;

public class EmptyPlayerNameException extends RuntimeException {
    public EmptyPlayerNameException() {}
    
    public EmptyPlayerNameException(String msg) {
        super(msg);
    }
}