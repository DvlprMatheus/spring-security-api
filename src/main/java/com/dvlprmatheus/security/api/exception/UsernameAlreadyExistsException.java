package com.dvlprmatheus.security.api.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    
    public UsernameAlreadyExistsException(String username) {
        super("Username already in use: " + username);
    }
}

