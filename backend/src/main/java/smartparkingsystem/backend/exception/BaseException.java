package smartparkingsystem.backend.exception;

import lombok.Getter;

/**
 * Base exception class for all custom application exceptions.
 * Includes error code and message for consistent error handling.
 */
@Getter
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;

    public BaseException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public BaseException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}

