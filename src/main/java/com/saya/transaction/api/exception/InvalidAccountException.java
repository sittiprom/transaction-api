package com.saya.transaction.api.exception;

public class InvalidAccountException extends RuntimeException  {
    public InvalidAccountException(String message) {
        super(message);
    }
}
