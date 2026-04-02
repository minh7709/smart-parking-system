package smartparkingsystem.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.repository.PricingRuleRepository;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // 2. Nếu chưa có, tạo tài khoản Admin mặc định
            User admin = new User();
            admin.setUsername("admin");

            // QUAN TRỌNG: Phải mã hóa mật khẩu trước khi lưu
            admin.setPassword(passwordEncoder.encode("12345678Aa"));

            admin.setRole(UserRole.ADMIN);
            admin.setFullName("System Administrator");
            admin.setPhone("0123456789");
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);

            for (VehicleTypeEnum vehicleType : VehicleTypeEnum.values()) {
                PricingRule defaultRule = new PricingRule();
                defaultRule.setRuleName("Default Flat Rate - " + vehicleType.name());
                defaultRule.setVehicleType(vehicleType);
                defaultRule.setStrategy(PricingStrategyEnum.FLAT_RATE);

                // Thiết lập giá cơ bản tùy theo loại xe
                BigInteger basePrice;
                switch (vehicleType) {
                    case CAR:
                        basePrice = BigInteger.valueOf(30000);
                        break;
                    case MOTO:
                        basePrice = BigInteger.valueOf(5000);
                        break;
                    case BICYCLE:
                    default:
                        basePrice = BigInteger.valueOf(2000);
                        break;
                }
                defaultRule.setBasePrice(basePrice);

                defaultRule.setStartTime(LocalDateTime.now());
                defaultRule.setActive(true);
                defaultRule.setCreatedAt(LocalDateTime.now());
                defaultRule.setCreator(admin);
                defaultRule.setPenaltyFee(BigInteger.valueOf(200));

                pricingRuleRepository.save(defaultRule);
            }
        }
    }
}