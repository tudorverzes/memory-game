package org.vv.exception;

public class MaxAttemptsExceededException extends RuntimeException {
    public MaxAttemptsExceededException() {}
    
    public MaxAttemptsExceededException(String msg) {
        super(msg);
    }
}