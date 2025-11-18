package org.vv.exception;

public class InvalidDifficultyException extends RuntimeException {
    public InvalidDifficultyException() {}
    
    public InvalidDifficultyException(String msg) {
        super(msg);
    }
}