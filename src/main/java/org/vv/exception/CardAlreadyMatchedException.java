package org.vv.exception;

public class CardAlreadyMatchedException extends RuntimeException {
    public CardAlreadyMatchedException() {}
    
    public CardAlreadyMatchedException(String msg) {
        super(msg);
    }
}