package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                "DUPLICATE_RESOURCE",
                String.format("%s đã tồn tại với %s: %s", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT.value()
        );
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue, String path) {
        super(
                "DUPLICATE_RESOURCE",
                String.format("%s đã tồn tại với %s: %s", resourceName, fieldName, fieldValue),
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

