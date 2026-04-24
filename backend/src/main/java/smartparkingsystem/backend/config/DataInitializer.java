package smartparkingsystem.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.*;
import smartparkingsystem.backend.repository.LaneRepository;
import smartparkingsystem.backend.repository.SubscriptionPricingRepository;
import smartparkingsystem.backend.repository.UserRepository;
import smartparkingsystem.backend.repository.PricingRuleRepository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;

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

    @Autowired
    private SubscriptionPricingRepository subscriptionPricingRepository;

    @Override
    public void run(@NonNull String... args) throws Exception {
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

            User guard = new User();
            guard.setUsername("guard");
            guard.setPassword(passwordEncoder.encode("12345678Aa"));
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

        // Seed missing pairs (vehicle_type + duration_type) so each enum combination is unique and complete.
        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        Set<String> existingPairs = new HashSet<>();
        subscriptionPricingRepository.findAll().forEach(item ->
                existingPairs.add(item.getVehicleType().name() + "|" + item.getDurationType().name())
        );

        for (VehicleTypeEnum vehicleType : VehicleTypeEnum.values()) {
            for (SubType durationType : SubType.values()) {
                String pairKey = vehicleType.name() + "|" + durationType.name();
                if (existingPairs.contains(pairKey)) {
                    continue;
                }

                SubscriptionPricing pricing = new SubscriptionPricing();
                pricing.setPricingName(buildPricingName(vehicleType, durationType));
                pricing.setVehicleType(vehicleType);
                pricing.setDurationType(durationType);
                pricing.setPrice(calculatePrice(vehicleType, durationType));
                pricing.setDescription("Auto-seeded default subscription pricing");
                pricing.setActive(true);
                pricing.setCreator(admin);

                subscriptionPricingRepository.save(pricing);
            }
        }
    }

    private String buildPricingName(VehicleTypeEnum vehicleType, SubType durationType) {
        return durationType.name() + " " + vehicleType.name() + " Subscription";
    }

    private BigInteger calculatePrice(VehicleTypeEnum vehicleType, SubType durationType) {
        BigInteger monthlyBase;
        switch (vehicleType) {
            case CAR:
                monthlyBase = BigInteger.valueOf(500000);
                break;
            case MOTOR:
                monthlyBase = BigInteger.valueOf(80000);
                break;
            case BICYCLE:
            default:
                monthlyBase = BigInteger.valueOf(50000);
                break;
        }

        int months;
        switch (durationType) {
            case QUARTERLY:
                months = 3;
                break;
            case YEARLY:
                months = 12;
                break;
            case MONTHLY:
            default:
                months = 1;
                break;
        }

        return monthlyBase.multiply(BigInteger.valueOf(months));
    }
}