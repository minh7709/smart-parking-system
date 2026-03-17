package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails.
 * HTTP Status: 400 Bad Request
 */
public class ValidationException extends BaseException {

    // Constructor for simple message
    public ValidationException(String message) {
        super(
                "VALIDATION_FAILED",
                message,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    // Constructor for field-specific validation error with path
    public ValidationException(String fieldName, String reason, String path) {
        super(
                "VALIDATION_FAILED",
                String.format("Validation failed for field '%s': %s", fieldName, reason),
                HttpStatus.BAD_REQUEST.value(),
                path
        );
    }

    // Constructor for field-specific validation error (without path)
    public ValidationException(String fieldName, String reason) {
        super(
                "VALIDATION_FAILED",
                String.format("Validation failed for field '%s': %s", fieldName, reason),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    // Constructor for message with path
    public ValidationException(String message, String path, boolean withPath) {
        super(
                "VALIDATION_FAILED",
                message,
                HttpStatus.BAD_REQUEST.value(),
                path
        );
    }
}

