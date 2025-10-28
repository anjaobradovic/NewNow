package rs.ftn.newnow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        log.warn("Missing required request part: {}", ex.getRequestPartName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Required part '" + ex.getRequestPartName() + "' is missing");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.warn("Validation error on field '{}': {}", fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation failed");
        response.put("errors", errors);
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        Map<String, String> response = new HashMap<>();
        response.put("message", "An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
