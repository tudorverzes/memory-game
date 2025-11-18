package org.vv.exception;

public class GameInterruptedException extends RuntimeException {
    public GameInterruptedException() {}
    
    public GameInterruptedException(String msg) {
        super(msg);
    }
}