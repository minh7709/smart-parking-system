package smartparkingsystem.backend.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import smartparkingsystem.backend.dto.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for all REST controllers.
 * Handles custom exceptions and Spring validation exceptions.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Handle custom BaseException and its subclasses.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex, WebRequest request) {
        log.warn("BaseException caught: {} - {}", ex.getErrorCode(), ex.getMessage()); //
        String path = request.getDescription(false).replace("uri=", ""); //

        ApiResponse<Object> response = ApiResponse.error(
                ex.getErrorCode(), //
                ex.getMessage(), //
                path
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(response); //
    }

    /**
     * Handle Spring validation errors (e.g., @Valid annotation failures).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getBindingResult().getErrorCount()); //

        List<ApiResponse.FieldError> fieldErrors = new ArrayList<>();

        // Map lỗi từ Spring sang cấu trúc của ApiResponse
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField(); //
            String message = error.getDefaultMessage(); //
            Object rejectedValue = ((FieldError) error).getRejectedValue(); //

            fieldErrors.add(ApiResponse.FieldError.builder()
                    .field(fieldName)
                    .message(message)
                    .rejectedValue(rejectedValue)
                    .build());
        });

        String path = request.getDescription(false).replace("uri=", ""); //

        // Sử dụng helper method mới thêm vào ApiResponse
        ApiResponse<Object> response = ApiResponse.error(
                "VALIDATION_FAILED",
                "One or more validation errors occurred",
                path,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle all other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected exception occurred", ex); //
        String path = request.getDescription(false).replace("uri=", ""); //

        ApiResponse<Object> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR", //
                "An unexpected error occurred. Please try again later.", //
                path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");

        ApiResponse<Object> response = ApiResponse.error(
                "FORBIDDEN",
                "Bạn không có quyền thực hiện hành động này. Chỉ dành cho admin.",
                path
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    /**
     * Handle 404 Not Found (Sai URL)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NoResourceFoundException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiResponse<Object> response = ApiResponse.error(
                "NOT_FOUND",
                "Đường dẫn không tồn tại: " + ex.getResourcePath(),
                path
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle 405 Method Not Allowed (Sai phương thức HTTP, ví dụ gọi GET cho API POST)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ApiResponse<Object> response = ApiResponse.error(
                "METHOD_NOT_ALLOWED",
                "Phương thức HTTP không được hỗ trợ. Vui lòng dùng: " + ex.getSupportedHttpMethods(),
                path
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Handle NullPointerException explicitly to alert developers of bugs.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException ex, WebRequest request) {
        // BẮT BUỘC: Log toàn bộ lỗi ra console/file để developer debug
        log.error("CRITICAL BUG - NullPointerException occurred: ", ex);

        String path = request.getDescription(false).replace("uri=", "");

        // TRẢ VỀ CLIENT: Thông báo chung chung, an toàn
        ApiResponse<Object> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống nghiêm trọng. Đội ngũ kỹ thuật đã được thông báo.",
                path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}