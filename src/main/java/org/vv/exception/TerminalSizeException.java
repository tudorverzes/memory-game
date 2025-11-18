package org.vv.exception;

public class TerminalSizeException extends RuntimeException {
    public TerminalSizeException() {}
    
    public TerminalSizeException(String msg) {
        super(msg);
    }
}