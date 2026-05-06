package smartparkingsystem.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.*;
import smartparkingsystem.backend.repository.LaneRepository;
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

    @Autowired
    private LaneRepository laneRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // 2. Nếu chưa có, tạo tài khoản Admin mặc định
            User admin = new User();
            admin.setUsername("admin");

            // QUAN TRỌNG: Phải mã hóa mật khẩu trước khi lưu
            admin.setPassword(passwordEncoder.encode("123456Aa"));

            admin.setRole(UserRole.ADMIN);
            admin.setFullName("System Administrator");
            admin.setPhone("0123456789");
            admin.setStatus(UserStatus.ACTIVE);

            User guard = new User();
            guard.setUsername("guard");
            guard.setPassword(passwordEncoder.encode("123456Aa"));
            guard.setRole(UserRole.GUARD);
            guard.setFullName("System Guard");
            guard.setPhone("0987654321");
            guard.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin);
            userRepository.save(guard);
        }
        if(pricingRuleRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow(() -> new RuntimeException("Admin user not found"));
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
                    case MOTOR:
                        basePrice = BigInteger.valueOf(5000);
                        break;
                    default:
                        basePrice = BigInteger.valueOf(2000);
                        break;
                }
                defaultRule.setBasePrice(basePrice);

                defaultRule.setActive(true);
                defaultRule.setCreatedAt(LocalDateTime.now());
                defaultRule.setCreator(admin);
                defaultRule.setPenaltyFee(BigInteger.valueOf(200));

                pricingRuleRepository.save(defaultRule);
            }
        }
        if(laneRepository.count() == 0) {
            Lane entryLane = new Lane();
            entryLane.setLaneName("Lane 1 - Entry");
            entryLane.setLaneType(LaneTypeEnum.IN);
            entryLane.setStatus(LaneStatus.ACTIVE);
            entryLane.setIpCamera("192.168.100.1");
            laneRepository.save(entryLane);

            Lane exitLane = new Lane();
            exitLane.setLaneName("Lane 1 - Exit");
            exitLane.setLaneType(LaneTypeEnum.OUT);
            exitLane.setStatus(LaneStatus.ACTIVE);
            exitLane.setIpCamera("192.168.100.10");
            laneRepository.save(exitLane);
        }
    }
}