package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation cannot be performed due to invalid state.
 * HTTP Status: 409 Conflict
 */
public class InvalidStateException extends BaseException {
    public InvalidStateException(String message) {
        super(
                "INVALID_STATE",
                message,
                HttpStatus.CONFLICT.value()
        );
    }

    public InvalidStateException(String message, String path) {
        super(
                "INVALID_STATE",
                message,
                HttpStatus.CONFLICT.value(),
                path
        );
    }

    public InvalidStateException(String message, Throwable cause) {
        super(
                "INVALID_STATE",
                message,
                HttpStatus.CONFLICT.value(),
                cause
        );
    }
}
