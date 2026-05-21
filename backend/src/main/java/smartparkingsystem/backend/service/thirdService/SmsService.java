package smartparkingsystem.backend.service.thirdService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    @Value("${app.sms.enabled}")
    private boolean isSmsEnabled;

    public void sendSms(String toPhoneNumber, String otpCode) {
        String mockMessage = String.format(
                "\n==================================================\n" +
                    "💌 [MOCK SMS] \n" +
                    "📱 Tới:     %s \n" +
                    "🔑 OTP:    %s \n" +
                    "📝 Nội dung: Mã xác thực tài khoản của bạn là: %s \n" +
                    "==================================================\n",
                toPhoneNumber, otpCode, otpCode
        );

        // 2. In ra Console
        logger.info(mockMessage);

        // 3. (Tuỳ chọn) Giả lập độ trễ mạng
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}