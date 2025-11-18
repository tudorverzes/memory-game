package org.vv.exception;

public class UnicodeUnsupportedException extends RuntimeException {
    public UnicodeUnsupportedException() {}
    
    public UnicodeUnsupportedException(String msg) {
        super(msg);
    }
}