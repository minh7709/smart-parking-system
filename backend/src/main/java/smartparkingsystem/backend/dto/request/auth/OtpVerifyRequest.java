package smartparkingsystem.backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
public class OtpVerifyRequest {
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(message = "Số điện thoại không hợp lệ", regexp = "\\+?[0-9]{10,11}")
    private String phone;

    @NotBlank(message = "OTP is required")
    @Pattern(message = "OTP must be a 6-digit number", regexp = "^\\d{6}$")
    private String otp;
}
