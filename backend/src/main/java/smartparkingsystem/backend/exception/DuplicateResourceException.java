package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a duplicate resource is attempted to be created.
 * HTTP Status: 409 Conflict
 */
public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                "DUPLICATE_RESOURCE",
                String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT.value()
        );
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue, String path) {
        super(
                "DUPLICATE_RESOURCE",
                String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT.value(),
                path
        );
    }

    public DuplicateResourceException(String message) {
        super(
                "DUPLICATE_RESOURCE",
                message,
                HttpStatus.CONFLICT.value()
        );
    }

    public DuplicateResourceException(String message, String path) {
        super(
                "DUPLICATE_RESOURCE",
                message,
                HttpStatus.CONFLICT.value(),
                path
        );
    }
}

