package smartparkingsystem.backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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


@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(@Nullable BaseException ex, WebRequest request) {
        log.warn("BaseException được bắt: {} - {}", ex != null ? ex.getErrorCode() : "UNKNOWN", ex != null ? ex.getMessage() : "Không có thông điệp");

        String path = request.getDescription(false).replace("uri=", "");

        if (ex != null && ex.getPath() == null) {
            ex.setPath(path);
        }

        ApiResponse<Object> response = ApiResponse.error(
                ex != null ? ex.getErrorCode() : "UNKNOWN",
                ex != null ? ex.getMessage() : "Đã xảy ra lỗi",
                ex != null && ex.getPath() != null ? ex.getPath() : path
        );

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex != null) {
            status = HttpStatus.valueOf(ex.getHttpStatus());
        }
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            @Nullable MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Lỗi xác thực dữ liệu: {}", ex != null ? ex.getBindingResult().getErrorCount() : "0"); //

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
                "Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra các lỗi chi tiết.",
                path,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            @Nullable HttpMessageNotReadableException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");
        String message;

        if (ex == null) {
            message = "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại dữ liệu gửi lên.";
        } else if (ex.getCause() instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty() ? "không rõ" : ife.getPath().get(0).getFieldName();
            String invalidValue = String.valueOf(ife.getValue());

            // Nếu là enum, hiển thị các giá trị hợp lệ
            if (ife.getTargetType().isEnum()) {
                Object[] enumConstants = ife.getTargetType().getEnumConstants();
                String validValues = Arrays.toString(enumConstants);
                message = String.format(
                        "enum không hợp lệ với trường '%s': '%s'. Dự liệu hợp lệ là: %s",
                        fieldName, invalidValue, validValues
                );
            } else {
                // Lỗi format khác (ví dụ: số, ngày, v.v.)
                message = String.format(
                        "Dữ liệu không hợp lệ '%s': '%s'. Dữ liệu mong đợi: %s",
                        fieldName, invalidValue, ife.getTargetType().getSimpleName()
                );
            }
        } else {
            // Lỗi JSON chung (malformed JSON)
            log.warn("Dự liệu yêu cầu không hợp lệ, vui lòng kiểm tra lại: {}", ex.getMessage());
            message = "Dự liệu yêu cầu không hợp lệ, vui lòng kiểm tra lại.";
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
        log.error("Đã xảy ra lỗi không mong muốn", ex); //
        String path = request.getDescription(false).replace("uri=", ""); //

        ApiResponse<Object> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR", //
                "Lỗi hệ thống.", //
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
                "Bạn không có quyền thực hiện hành động này",
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
     * Handle database constraint violations (NOT NULL, UNIQUE, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(@Nullable DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Vi phạm ràng buộc dữ liệu: {}", ex != null ? ex.getMessage() : "Không rõ");

        String path = request.getDescription(false).replace("uri=", "");
        String message = "Xung đột rằng buộc dữ liệu";

        if (ex != null && ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage != null) {
                // Extract constraint name from error message
                if (causeMessage.contains("NOT NULL")) {
                    message = "Một hoặc nhiều trường bắt buộc không được cung cấp";
                } else if (causeMessage.contains("value too long")
                        || causeMessage.contains("Data too long")
                        || causeMessage.contains("too long for column")) {
                    message = "Dữ liệu quá dài cho một trường nào đó. Vui lòng kiểm tra lại.";
                } else if (causeMessage.contains("UNIQUE") || causeMessage.contains("unique")) {
                    message = "Dữ liệu đã tồn tại và phải là duy nhất. Vui lòng kiểm tra lại.";
                } else if (causeMessage.contains("FOREIGN KEY") || causeMessage.contains("foreign key")) {
                    message = "Dữ liệu liên quan không tồn tại. Vui lòng kiểm tra lại các tham chiếu.";
                } else {
                    message = "Xung đột rằng buộc dữ liệu: " + causeMessage;
                }
            }
        }

        ApiResponse<Object> response = ApiResponse.error(
                "DATABASE_CONSTRAINT_VIOLATION",
                message,
                path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(@Nullable NullPointerException ex, WebRequest request) {
        log.error("Lỗi nghiêm trọng - xảy ra NullPointerException: ", ex);

        String path = request.getDescription(false).replace("uri=", "");

        ApiResponse<Object> response = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống nghiêm trọng. Đội ngũ kỹ thuật đã được thông báo.",
                path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}