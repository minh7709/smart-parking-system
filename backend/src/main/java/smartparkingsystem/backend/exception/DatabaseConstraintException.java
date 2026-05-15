package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a database constraint is violated.
 * Handles NOT NULL violations, unique constraint violations, and other DB constraints.
 * HTTP Status: 400 Bad Request
 */
public class DatabaseConstraintException extends BaseException {
    public DatabaseConstraintException(String message) {
        super(
                "DATABASE_CONSTRAINT_VIOLATION",
                message,
                HttpStatus.BAD_REQUEST.value()
        );
    }

    public DatabaseConstraintException(String message, String path) {
        super(
                "DATABASE_CONSTRAINT_VIOLATION",
                message,
                HttpStatus.BAD_REQUEST.value(),
                path
        );
    }

    public DatabaseConstraintException(String fieldName, String constraintType) {
        super(
                "DATABASE_CONSTRAINT_VIOLATION",
                String.format("Field '%s' violated %s constraint", fieldName, constraintType),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    public DatabaseConstraintException(String message, Throwable cause) {
        super(
                "DATABASE_CONSTRAINT_VIOLATION",
                message,
                HttpStatus.BAD_REQUEST.value(),
                cause
        );
    }
}
