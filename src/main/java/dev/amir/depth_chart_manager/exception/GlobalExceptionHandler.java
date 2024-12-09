package dev.amir.depth_chart_manager.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String constraintName = extractConstraintName(ex);
        String errorMessage = "Unique constraint violation: " + constraintName;
        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        return message.substring(0, message.indexOf("SQL statement"));
    }
}

