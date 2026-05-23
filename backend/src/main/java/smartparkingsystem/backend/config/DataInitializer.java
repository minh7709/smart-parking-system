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
import smartparkingsystem.backend.exception.ResourceNotFoundException;
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
import smartparkingsystem.backend.service.guard.InvoiceService;

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
    @Autowired
    private InvoiceService invoiceService;

    @Override
    public void run(@NonNull String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
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

        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy Admin user"));
        User guard = userRepository.findByUsername("guard")
                .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy Guard user"));

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
                pricing.setDescription("sinh tự động cấu hình vé đăng ký");
                pricing.setActive(true);
                pricing.setCreator(admin);

                subscriptionPricingRepository.save(pricing);
            }
        }
        System.out.println("SubscriptionPricing được sinh thành công, tổng là: " + subscriptionPricingRepository.count());
        // Seed sample Vehicles
        if (vehicleRepository.count() == 0) {
            Vehicle vehicle1 = Vehicle.builder()
                    .licensePlate("29A12345")
                    .vehicleType(VehicleTypeEnum.CAR)
                    .brand("Toyota")
                    .customerName("Nguyen Van A")
                    .customerPhone("0901234567")
                    .deleted(false)
                    .build();
            vehicleRepository.save(vehicle1);

            Vehicle vehicle2 = Vehicle.builder()
                    .licensePlate("59P166480")
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
                    Invoice invoice = invoiceService.createInvoiceForSubscription(subscription1, guard, PaymentMethod.CASH);
                    invoiceService.updateInvoiceStatus(invoice, PaymentStatus.SUCCESS);
                }
            }

            // Seed sample ParkingSessions
            if (parkingSessionRepository.count() == 0 && entryLane != null) {
                // Session 1: IN_PARKING
                ParkingSession session1 = ParkingSession.builder()
                        .entryLane(entryLane)
                        .vehicleType(VehicleTypeEnum.CAR)
                        .timeIn(LocalDateTime.now().minusHours(2))
                        .plateInOcr("50AA37979")
                        .finalPlate("50AA37979")
                        .confidenceIn(0.95f)
                        .month(false)
                        .status(SessionStatus.PARKED)
                        .imageInUrl("check-in/7a05a4a6-7b81-4d7e-8af6-36dd72d20d81.jpg")
                        .build();
                parkingSessionRepository.save(session1);

                // Session 2: COMPLETED
                ParkingSession session2 = ParkingSession.builder()
                        .entryLane(entryLane)
                        .exitLane(exitLane)
                        .vehicleType(VehicleTypeEnum.MOTOR)
                        .timeIn(LocalDateTime.now().minusHours(5))
                        .timeOut(LocalDateTime.now().minusHours(1))
                        .plateInOcr("29AB22658")
                        .plateOutOcr("29AB22658")
                        .finalPlate("29AB22658")
                        .confidenceIn(0.98f)
                        .confidenceOut(0.97f)
                        .month(false)
                        .status(SessionStatus.COMPLETED)
                        .imageInUrl("check-in/c76b48ae-6418-45cd-b4d9-976d15fd02bd.jpg")
                        .imageOutUrl("check-out/c76b48ae-6418-45cd-b4d9-976d15fd02bd.jpg")
                        .build();
                parkingSessionRepository.save(session2);

                ParkingSession session3 = ParkingSession.builder()
                        .entryLane(entryLane)
                        .exitLane(exitLane)
                        .vehicleType(VehicleTypeEnum.CAR)
                        .timeIn(LocalDateTime.now().minusHours(3))
                        .plateInOcr("73K99999")
                        .finalPlate("73K99999")
                        .confidenceIn(0.90f)
                        .month(false)
                        .status(SessionStatus.PARKED)
                        .imageInUrl("check-in/f4db84fd-8be9-4e47-9cf0-8ef89d699924.jpg")
                        .build();
                parkingSessionRepository.save(session3);

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
                            .description("Có va chạm xảy ra")
                            .incidentType(IncidentTypeEnum.DAMAGE)
                            .evidenceUrl("evidence/7f81805e-5ea7-4095-b996-188fce6f043c.jpg")
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