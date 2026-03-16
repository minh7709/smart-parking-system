package smartparkingsystem.backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
public class ResetPasswordRequest {
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số",
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    private String newPassword;

    @NotBlank(message = "Token không được để trống")
    private String token;
}
