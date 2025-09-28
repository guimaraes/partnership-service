package br.com.ecomercial.partnership.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = Map.of();
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(String field, String errorMessage) {
        super(String.format("Field '%s' validation failed: %s", field, errorMessage));
        this.fieldErrors = Map.of(field, errorMessage);
    }
}
