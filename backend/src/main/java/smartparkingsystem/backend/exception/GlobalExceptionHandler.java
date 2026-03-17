package smartparkingsystem.backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import java.util.Arrays;
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
    public ResponseEntity<ApiResponse<Object>> handleBaseException(@Nullable BaseException ex, WebRequest request) {
        log.warn("BaseException caught: {} - {}", ex != null ? ex.getErrorCode() : "UNKNOWN", ex != null ? ex.getMessage() : "No message");

        String path = request.getDescription(false).replace("uri=", "");

        // Set path if not already set
        if (ex != null && ex.getPath() == null) {
            ex.setPath(path);
        }

        ApiResponse<Object> response = ApiResponse.error(
                ex != null ? ex.getErrorCode() : "UNKNOWN",
                ex != null ? ex.getMessage() : "An error occurred",
                ex != null && ex.getPath() != null ? ex.getPath() : path
        );

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex != null) {
            status = HttpStatus.valueOf(ex.getHttpStatus());
        }
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handle Spring validation errors (e.g., @Valid annotation failures).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            @Nullable MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {}", ex != null ? ex.getBindingResult().getErrorCount() : "0"); //

        List<ApiResponse.FieldError> fieldErrors = new ArrayList<>();

        // Map lỗi từ Spring sang cấu trúc của ApiResponse
        if (ex != null) {
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
        }

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
     * Handle Jackson mapping errors - Invalid enum values or malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            @Nullable HttpMessageNotReadableException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");
        String message;

        if (ex == null) {
            message = "Malformed JSON request. Please check your request body.";
        } else if (ex.getCause() instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty() ? "unknown" : ife.getPath().get(0).getFieldName();
            String invalidValue = String.valueOf(ife.getValue());

            // Nếu là enum, hiển thị các giá trị hợp lệ
            if (ife.getTargetType().isEnum()) {
                Object[] enumConstants = ife.getTargetType().getEnumConstants();
                String validValues = Arrays.toString(enumConstants);
                message = String.format(
                        "Invalid enum value for field '%s': '%s'. Valid values are: %s",
                        fieldName, invalidValue, validValues
                );
                log.warn("Invalid enum value received: field={}, value={}, target={}",
                        fieldName, invalidValue, ife.getTargetType().getSimpleName());
            } else {
                // Lỗi format khác (ví dụ: số, ngày, v.v.)
                message = String.format(
                        "Invalid value for field '%s': '%s'. Expected type: %s",
                        fieldName, invalidValue, ife.getTargetType().getSimpleName()
                );
                log.warn("Invalid format received: field={}, value={}, target={}",
                        fieldName, invalidValue, ife.getTargetType().getSimpleName());
            }
        } else {
            // Lỗi JSON chung (malformed JSON)
            log.warn("Malformed JSON or invalid request format: {}", ex.getMessage());
            message = "Malformed JSON request. Please check your request body.";
        }

        ApiResponse<Object> response = ApiResponse.error(
                "BAD_REQUEST",
                message,
                path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle all other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(@Nullable Exception ex, WebRequest request) {
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
    public ResponseEntity<?> handleAccessDeniedException(
            @Nullable AccessDeniedException ex, WebRequest request) {

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
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(@Nullable NoResourceFoundException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String resourcePath = ex != null ? ex.getResourcePath() : "unknown";
        ApiResponse<Object> response = ApiResponse.error(
                "NOT_FOUND",
                "Đường dẫn không tồn tại: " + resourcePath,
                path
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle 405 Method Not Allowed (Sai phương thức HTTP, ví dụ gọi GET cho API POST)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(@Nullable HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String supportedMethods = ex != null && ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods().toString() : "GET, POST, PUT, DELETE";
        ApiResponse<Object> response = ApiResponse.error(
                "METHOD_NOT_ALLOWED",
                "Phương thức HTTP không được hỗ trợ. Vui lòng dùng: " + supportedMethods,
                path
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Handle NullPointerException explicitly to alert developers of bugs.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(@Nullable NullPointerException ex, WebRequest request) {
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