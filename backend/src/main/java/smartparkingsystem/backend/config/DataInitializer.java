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
import smartparkingsystem.backend.repository.IncidentRepository;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.repository.VehicleRepository;
import smartparkingsystem.backend.entity.Vehicle;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Incident;

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

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ParkingSessionRepository parkingSessionRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public void run(@NonNull String... args) throws Exception {
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

        Lane entryLane = laneRepository.findAll().stream().filter(l -> l.getLaneType() == LaneTypeEnum.IN).findFirst().orElse(null);
        Lane exitLane = laneRepository.findAll().stream().filter(l -> l.getLaneType() == LaneTypeEnum.OUT).findFirst().orElse(null);

        // Seed missing pairs (vehicle_type + duration_type) so each enum combination is unique and complete.
        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        User guard = userRepository.findByUsername("guard")
                .orElseThrow(() -> new RuntimeException("Guard user not found"));

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
        System.out.println("SubscriptionPricing seeding completed. Total records: " + subscriptionPricingRepository.count());
        // Seed sample Vehicles
        if (vehicleRepository.count() == 0) {
            Vehicle vehicle1 = Vehicle.builder()
                    .licensePlate("29A-123.45")
                    .vehicleType(VehicleTypeEnum.CAR)
                    .brand("Toyota")
                    .customerName("Nguyen Van A")
                    .customerPhone("0901234567")
                    .deleted(false)
                    .build();
            vehicleRepository.save(vehicle1);

            Vehicle vehicle2 = Vehicle.builder()
                    .licensePlate("59P1-664.80")
                    .vehicleType(VehicleTypeEnum.MOTOR)
                    .brand("Honda")
                    .customerName("Le Thi B")
                    .customerPhone("0909876543")
                    .deleted(false)
                    .build();
            vehicleRepository.save(vehicle2);

            // Seed sample Subscriptions
            if (subscriptionRepository.count() == 0) {
                SubscriptionPricing subPricingCar = subscriptionPricingRepository.findAll().stream()
                        .filter(p -> p.getVehicleType() == VehicleTypeEnum.CAR && p.getDurationType() == SubType.MONTHLY)
                        .findFirst().orElse(null);
                System.out.println(subPricingCar + "1");
                if (subPricingCar != null) {
                    Subscription subscription1 = Subscription.builder()
                            .vehicle(vehicle1)
                            .subscriptionPricing(subPricingCar)
                            .price(subPricingCar.getPrice())
                            .startDate(LocalDateTime.now().minusDays(5))
                            .endDate(LocalDateTime.now().plusDays(25))
                            .status(SubStatus.ACTIVE)
                            .build();
                    subscriptionRepository.save(subscription1);
                }
            }

            // Seed sample ParkingSessions
            if (parkingSessionRepository.count() == 0 && entryLane != null) {
                // Session 1: IN_PARKING
                ParkingSession session1 = ParkingSession.builder()
                        .entryLane(entryLane)
                        .vehicleType(VehicleTypeEnum.CAR)
                        .timeIn(LocalDateTime.now().minusHours(2))
                        .plateInOcr("29A12345")
                        .finalPlate("29A-123.45")
                        .confidenceIn(0.95f)
                        .month(true)
                        .status(SessionStatus.PARKED)
                        .build();
                parkingSessionRepository.save(session1);

                // Session 2: COMPLETED
                ParkingSession session2 = ParkingSession.builder()
                        .entryLane(entryLane)
                        .exitLane(exitLane)
                        .vehicleType(VehicleTypeEnum.MOTOR)
                        .timeIn(LocalDateTime.now().minusHours(5))
                        .timeOut(LocalDateTime.now().minusHours(1))
                        .plateInOcr("59P166480")
                        .plateOutOcr("59P166480")
                        .finalPlate("59P1-664.80")
                        .confidenceIn(0.98f)
                        .confidenceOut(0.97f)
                        .month(false)
                        .status(SessionStatus.COMPLETED)
                        .build();
                parkingSessionRepository.save(session2);

                // Seed sample Invoices
                if (invoiceRepository.count() == 0) {
                    Invoice invoice1 = Invoice.builder()
                            .invoiceType(InvoiceTypeEnum.PARKING_FEE)
                            .parkingSession(session2)
                            .parkingAmount(BigInteger.valueOf(5000))
                            .totalAmount(BigInteger.valueOf(5000))
                            .cashier(guard)
                            .paymentMethod(PaymentMethod.CASH)
                            .paymentTime(LocalDateTime.now().minusHours(1))
                            .status(PaymentStatus.SUCCESS)
                            .build();
                    invoiceRepository.save(invoice1);
                }

                // Seed sample Incident
                if (incidentRepository.count() == 0) {
                    Incident incident1 = Incident.builder()
                            .parkingSession(session2)
                            .reporter(guard)
                            .description("Biển số mờ, cần kiểm tra lại camera.")
                            .incidentType(IncidentTypeEnum.WRONG_PLATE)
                            .build();
                    incidentRepository.save(incident1);
                }
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