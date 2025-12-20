package com.dvlprmatheus.security.api.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found: %s", resource, identifier));
    }
}

