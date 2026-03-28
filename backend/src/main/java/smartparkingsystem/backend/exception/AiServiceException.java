package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when AI service integration fails.
 * This includes file reading errors, network errors, or AI service failures.
 */
public class AiServiceException extends BaseException {

    public AiServiceException(String message) {
        super(
            "AI_SERVICE_ERROR",
            message,
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }

    public AiServiceException(String message, Throwable cause) {
        super(
            "AI_SERVICE_ERROR",
            message,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            cause
        );
    }

    public AiServiceException(String message, String path) {
        super(
            "AI_SERVICE_ERROR",
            message,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            path
        );
    }

    public AiServiceException(String message, Throwable cause, String path) {
        super(
            "AI_SERVICE_ERROR",
            message,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            cause
        );
        this.setPath(path);
    }
}

