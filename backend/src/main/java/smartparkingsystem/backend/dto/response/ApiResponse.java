package smartparkingsystem.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Ẩn các trường null để JSON gọn gàng
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private String path;
    private LocalDateTime timestamp;

    // Thêm danh sách lỗi chi tiết cho các form validation
    private List<FieldError> fieldErrors;

    // Helper class để chứa thông tin lỗi của từng trường
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    // Helper cho thành công
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Helper cho lỗi thông thường
    public static <T> ApiResponse<T> error(String errorCode, String message, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Helper cho lỗi Validation (có kèm fieldErrors)
    public static <T> ApiResponse<T> error(String errorCode, String message, String path, List<FieldError> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .fieldErrors(fieldErrors) // Đưa danh sách lỗi vào
                .timestamp(LocalDateTime.now())
                .build();
    }
}