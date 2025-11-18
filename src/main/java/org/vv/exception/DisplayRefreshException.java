package org.vv.exception;

public class DisplayRefreshException extends RuntimeException {
    public DisplayRefreshException() {}
    
    public DisplayRefreshException(String msg) {
        super(msg);
    }
}