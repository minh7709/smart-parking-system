package smartparkingsystem.backend.service.thirdService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int OTP_TTL = 3; // phút
    private static final int RESET_TOKEN_TTL = 3; // phút - token reset password có hiệu lực 10 phút

    /**
     * Tạo OTP cho identifier (phone hoặc email)
     * @param identifier số điện thoại hoặc email
     * @param purpose mục đích (REGISTER, FORGOT_PASSWORD, etc.)
     */
    public String generateOtp(String identifier, String purpose) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        String key = buildOtpKey(identifier, purpose);
        redisTemplate.opsForValue().set(key, otp, OTP_TTL, TimeUnit.MINUTES);
        return otp;
    }

    /**
     * Validate OTP cho identifier (phone hoặc email)
     */
    public boolean validateOtp(String identifier, String inputOtp, String purpose) {
        String key = buildOtpKey(identifier, purpose);
        Object cachedOtp = redisTemplate.opsForValue().get(key);
        if (cachedOtp != null && cachedOtp.toString().equals(inputOtp)) {
            redisTemplate.delete(key); // Xóa ngay sau khi dùng xong (chỉ dùng 1 lần)
            return true;
        }
        return false;
    }

    private String buildOtpKey(String identifier, String purpose) {
        return "OTP:" + purpose + ":" + identifier;
    }

    // ===== RESET PASSWORD TOKEN =====

    /**
     * Tạo token ngẫu nhiên để reset password
     * Token này chỉ được tạo SAU KHI user đã verify OTP thành công
     * @param identifier số điện thoại hoặc email đã verify
     * @param type loại identifier ("phone" hoặc "email")
     * @return token UUID
     */
    public String generateResetToken(String identifier, String type) {
        String token = UUID.randomUUID().toString();
        String key = buildResetTokenKey(token);
        // Lưu identifier và type vào value (format: type:identifier)
        String value = type + ":" + identifier;
        redisTemplate.opsForValue().set(key, value, RESET_TOKEN_TTL, TimeUnit.MINUTES);
        return token;
    }

    /**
     * Tạo token cho phone (backward compatible)
     */
    public String generateResetToken(String phoneNumber) {
        return generateResetToken(phoneNumber, "phone");
    }

    /**
     * Validate reset token và trả về identifier nếu hợp lệ
     * @param token token cần validate
     * @return identifier (phone hoặc email) nếu token hợp lệ, null nếu không hợp lệ
     */
    public String validateResetToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String key = buildResetTokenKey(token);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // Parse value (format: type:identifier)
        String valueStr = value.toString();
        if (valueStr.contains(":")) {
            return valueStr.substring(valueStr.indexOf(":") + 1);
        }
        return valueStr; // backward compatible với phone cũ
    }

    /**
     * Lấy loại identifier từ token (phone hoặc email)
     */
    public String getResetTokenType(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String key = buildResetTokenKey(token);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        String valueStr = value.toString();
        if (valueStr.contains(":")) {
            return valueStr.substring(0, valueStr.indexOf(":"));
        }
        return "phone"; // backward compatible
    }

    /**
     * Xóa reset token sau khi đã sử dụng (đổi password thành công)
     * @param token token cần xóa
     */
    public void deleteResetToken(String token) {
        if (token != null && !token.isEmpty()) {
            String key = buildResetTokenKey(token);
            redisTemplate.delete(key);
        }
    }

    private String buildResetTokenKey(String token) {
        return "RESET_TOKEN:" + token;
    }

    @Deprecated
    public String generateOtp(String phoneNumber) {
        return generateOtp(phoneNumber, "GENERAL");
    }

    @Deprecated
    public boolean validateOtp(String phoneNumber, String inputOtp) {
        return validateOtp(phoneNumber, inputOtp, "GENERAL");
    }
}