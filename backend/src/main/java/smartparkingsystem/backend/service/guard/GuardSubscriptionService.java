package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.dto.request.SubscriptionRequest;
import smartparkingsystem.backend.dto.response.SubscriptionResponse;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.entity.Vehicle;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.exception.DuplicateResourceException;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.SubscriptionMapper;
import smartparkingsystem.backend.mapper.SubscriptionPricingMapper;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.repository.VehicleRepository;
import smartparkingsystem.backend.service.admin.SubscriptionPricingService;
import smartparkingsystem.backend.service.auth.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuardSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPricingService subscriptionPricingService;
    private final VehicleRepository vehicleRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final SubscriptionPricingMapper subscriptionPricingMapper;
    private final UserService userService;

    public SubscriptionResponse createSubscription(SubscriptionRequest request){
        LocalDateTime now = LocalDateTime.now();
        if(request.getStartDate().isBefore(now)){
            throw new ValidationException("Ngày bắt đầu phải sau ngày hiện tại");
        }
        Vehicle vehicle = vehicleRepository.findByLicensePlateAndDeletedFalse(request.getLicensePlate())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với biển số xe: " + request.getLicensePlate()));
        Subscription subscription = subscriptionRepository
                .findByVehicleIdAndStatus(vehicle.getId(), SubStatus.ACTIVE)
                .orElse(null);

        if(subscription != null && subscription.getEndDate().isAfter(now)){
                 throw new DuplicateResourceException("Phương tiện đã có đăng ký đang hoạt động, vui lòng đăng ký sau thời gian hết hạn: " + subscription.getEndDate());
        } else {
            SubscriptionPricing subscriptionPricing  = subscriptionPricingService.
                    getSubscriptionPricingByDurationTypeAndVehicleType(request.getSubType(), vehicle.getVehicleType());
            LocalDateTime endDate = calculateEndDate(request.getStartDate(), request.getSubType());
            Subscription newSubscription = subscriptionMapper.toEntity(request, subscriptionPricing, vehicle, endDate);
            subscriptionRepository.save(newSubscription);

            invoiceService.createInvoiceForSubscription(newSubscription, userService.getCurrentUser(), request.getPaymentMethod());
            return subscriptionMapper.toResponse(newSubscription);
        }
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, SubType subType) {
        switch (subType) {
            case MONTHLY:
                return startDate.plusMonths(1);
            case QUARTERLY:
                return startDate.plusMonths(3);
            case YEARLY:
                return startDate.plusYears(1);
            default:
                throw new ValidationException("Invalid subscription type: " + subType);
        }
    }

    public SubscriptionResponse updateSubscription(SubscriptionRequest request, UUID subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký với ID: " + subscriptionId));
        if(subscription.getStatus() != SubStatus.PENDING){
            throw new InvalidStateException("chỉ thể cập nhật đăng ký đang được xử lý");
        }
        Vehicle vehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy phương tiện với biển số xe: " + request.getLicensePlate())
        );
        LocalDateTime now = LocalDateTime.now();
        if(request.getStartDate().isBefore(now)) {
            throw new ValidationException("Ngày bắt đầu phải sau ngày hiện tại");
        }
        if(!subscription.getStartDate().isEqual(request.getStartDate())){
            subscription.setStartDate(request.getStartDate());
            subscription.setEndDate(calculateEndDate(request.getStartDate(), request.getSubType()));
        }
        subscription.setVehicle(vehicle);

        subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(subscription);
    }

    public void deleteSubscription(UUID subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + subscriptionId));
        if(subscription.getStatus() != SubStatus.PENDING){
            throw new InvalidStateException("chỉ thể xóa đăng ký đang được xử lý");
        }
        subscriptionRepository.deleteById(subscriptionId);
        Invoice invoice = invoiceRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn cho đăng ký với ID: " + subscriptionId));
        invoiceService.updateInvoiceStatus(invoice, PaymentStatus.FAILED);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findAllByStatusAndEndDateBefore(SubStatus.ACTIVE, now);
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubStatus.EXPIRED);
        }
        subscriptionRepository.saveAll(expiredSubscriptions);
    }
    public SubscriptionResponse getSubscriptionById(UUID subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + subscriptionId));
        return subscriptionMapper.toResponse(subscription);
    }

    public SubscriptionResponse getSubscriptionByVehicleId(UUID vehicleId){
        Subscription subscription = subscriptionRepository.findByVehicleIdAndStatus(vehicleId, SubStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký hoạt động cho phương tiện với ID: " + vehicleId));
        return subscriptionMapper.toResponse(subscription);
    }

    public SubscriptionResponse getSubscriptionByLicensePlate(String licensePlate){
        Subscription subscription = subscriptionRepository.findByVehicle_LicensePlateIgnoreCaseAndStatus(licensePlate.trim().toLowerCase(), SubStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký hoạt động cho phương tiện với biển số: " + licensePlate));
        return subscriptionMapper.toResponse(subscription);
    }

    public Page<SubscriptionResponse> getSubscriptions(Pageable pageable, SubStatus subStatus, SubType subType) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Subscription> page;

        if (subStatus != null && subType != null) {
            page = subscriptionRepository.findAllByStatusAndSubscriptionPricing_DurationType(subStatus, subType, sortedPageable);
        } else if (subStatus != null) {
            page = subscriptionRepository.findAllByStatus(subStatus, sortedPageable);
        } else if (subType != null) {
            page = subscriptionRepository.findAllBySubscriptionPricing_DurationType(subType, sortedPageable);
        } else {
            page = subscriptionRepository.findAll(sortedPageable);
        }

        return page.map(subscriptionMapper::toResponse);
    }
}
