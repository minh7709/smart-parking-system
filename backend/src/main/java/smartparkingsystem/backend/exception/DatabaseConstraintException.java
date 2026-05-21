package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;


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

    public  DatabaseConstraintException(String resourceName, String fieldName, Object fieldValue, String path){
        super(
                "DATABASE_CONSTRAINT_VIOLATION",
                String.format("%s với %s '%s' đã tồn tại.", resourceName, fieldName, fieldValue),
                HttpStatus.BAD_REQUEST.value(),
                path
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
