package smartparkingsystem.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not authorized to perform an action.
 * HTTP Status: 403 Forbidden
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(
                "UNAUTHORIZED",
                message,
                HttpStatus.FORBIDDEN.value()
        );
    }

    public UnauthorizedException(String message, String path) {
        super(
                "UNAUTHORIZED",
                message,
                HttpStatus.FORBIDDEN.value(),
                path
        );
    }

    public UnauthorizedException(String resource, String action, String path) {
        super(
                "UNAUTHORIZED",
                String.format("You are not authorized to %s %s", action, resource),
                HttpStatus.FORBIDDEN.value(),
                path
        );
    }
}

