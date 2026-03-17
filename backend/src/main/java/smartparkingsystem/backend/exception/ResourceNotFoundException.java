package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * HTTP Status: 404 Not Found
 */
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                "RESOURCE_NOT_FOUND",
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, String path) {
        super(
                "RESOURCE_NOT_FOUND",
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND.value(),
                path
        );
    }

    public ResourceNotFoundException(String message) {
        super(
                "RESOURCE_NOT_FOUND",
                message,
                HttpStatus.NOT_FOUND.value()
        );
    }

    public ResourceNotFoundException(String message, String path) {
        super(
                "RESOURCE_NOT_FOUND",
                message,
                HttpStatus.NOT_FOUND.value(),
                path
        );
    }
}

