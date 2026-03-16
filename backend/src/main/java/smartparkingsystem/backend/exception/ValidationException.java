package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails.
 * HTTP Status: 400 Bad Request
 */
public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(
                "VALIDATION_FAILED",
                message,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    public ValidationException(String fieldName, String reason) {
        super(
                "VALIDATION_FAILED",
                String.format("Validation failed for field '%s': %s", fieldName, reason),
                HttpStatus.BAD_REQUEST.value()
        );
    }
}

