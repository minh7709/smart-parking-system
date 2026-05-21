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

    @NotBlank(message = "OTP là bắt buộc")
    @Pattern(message = "OTP phải gồm 6 chữ số", regexp = "^\\d{6}$")
    private String otp;
}
