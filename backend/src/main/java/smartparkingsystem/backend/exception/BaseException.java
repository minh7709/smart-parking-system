package smartparkingsystem.backend.exception;

import lombok.Getter;
import lombok.Setter;


@Getter
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;
    @Setter
    private String path; // Request path where error occurred

    public BaseException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.path = null;
    }

    public BaseException(String errorCode, String message, int httpStatus, String path) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.path = path;
    }

    public BaseException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.path = null;
    }

}

