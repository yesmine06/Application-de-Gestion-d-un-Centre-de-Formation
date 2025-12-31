package com.formation.exception;

/**
 * Exception pour les erreurs de validation
 */
public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super(message);
    }
}

