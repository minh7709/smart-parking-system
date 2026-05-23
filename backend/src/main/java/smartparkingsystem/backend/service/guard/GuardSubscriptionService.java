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
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.repository.VehicleRepository;
import smartparkingsystem.backend.service.admin.SubscriptionPricingService;
import smartparkingsystem.backend.service.auth.UserService;

import java.math.BigInteger;
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
    private final UserService userService;

    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request){
        LocalDateTime now = LocalDateTime.now();
        if(request.getStartDate().isBefore(now)){
            throw new ValidationException("Ngày bắt đầu phải sau ngày hiện tại");
        }
        Vehicle vehicle = vehicleRepository.findByLicensePlateAndDeletedFalse(request.getLicensePlate())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với biển số xe: " + request.getLicensePlate()));
        Subscription subscription = subscriptionRepository
                .findFirstByVehicleIdAndStatusIn(vehicle.getId(), List.of(SubStatus.ACTIVE, SubStatus.PENDING))
                .orElse(null);

        if(subscription != null && subscription.getEndDate().isAfter(request.getStartDate())){
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
                throw new ValidationException("Loại đăng ký không hợp lệ: " + subType);
        }
    }
    @Transactional
    public SubscriptionResponse updateSubscription(SubscriptionRequest request, UUID subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + subscriptionId));
        if(subscription.getStatus() != SubStatus.PENDING){
            throw new InvalidStateException("chỉ thể cập nhật đăng ký đang được xử lý");
        }
        Vehicle vehicle = vehicleRepository.findByLicensePlateAndDeletedFalse(request.getLicensePlate()).orElseThrow(
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

        if(!request.getSubType().equals(subscription.getSubscriptionPricing().getDurationType())){
            Invoice invoice = invoiceRepository.findBySubscriptionId(subscriptionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn cho đăng ký với ID: " + subscriptionId));

            SubscriptionPricing subscriptionPricing  = subscriptionPricingService.
                    getSubscriptionPricingByDurationTypeAndVehicleType(request.getSubType(), vehicle.getVehicleType());
            subscription.setSubscriptionPricing(subscriptionPricing);
            subscription.setPrice(subscriptionPricing.getPrice());
            invoiceService.updateInvoiceAmount(invoice, BigInteger.ZERO, BigInteger.ZERO, subscriptionPricing.getPrice());
        }
        subscription.setVehicle(vehicle);
        subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(subscription);
    }
    @Transactional
    public void deleteSubscription(UUID subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký với ID: " + subscriptionId));
        if(subscription.getStatus() != SubStatus.PENDING){
            throw new InvalidStateException("chỉ thể xóa đăng ký đang được xử lý");
        }
        subscription.setStatus(SubStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        Invoice invoice = invoiceRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException ("Không tìm thấy hóa đơn cho đăng ký với ID: " + subscriptionId));
        invoiceService.updateInvoiceStatus(invoice, PaymentStatus.FAILED);
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
        Subscription subscription = subscriptionRepository.findByVehicle_LicensePlateIgnoreCaseAndStatus(licensePlate.trim(), SubStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký hoạt động cho phương tiện với biển số: " + licensePlate));
        return subscriptionMapper.toResponse(subscription);
    }

    public Page<SubscriptionResponse> getSubscriptions(Pageable pageable, SubStatus subStatus, SubType subType, String licensePlate) {
        String trimmedLicensePlate = licensePlate != null ? licensePlate.trim() : null;

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Subscription> page;

        // Xác định repository method dựa trên sự kết hợp của các filter
        boolean hasLicensePlate = trimmedLicensePlate != null && !trimmedLicensePlate.isBlank();
        boolean hasStatus = subStatus != null;
        boolean hasSubType = subType != null;

        // Sử dụng partial search (LIKE) cho licensePlate
        if (hasLicensePlate && hasStatus && hasSubType) {
            // Tất cả 3 filter
            page = subscriptionRepository.findByVehicleLicensePlateContainingIgnoreCaseAndStatusAndDurationType(
                    trimmedLicensePlate, subStatus, subType, sortedPageable);
        } else if (hasLicensePlate && hasStatus) {
            // Có licensePlate và status
            page = subscriptionRepository.findByVehicleLicensePlateContainingIgnoreCaseAndStatus(
                    trimmedLicensePlate, subStatus, sortedPageable);
        } else if (hasLicensePlate && hasSubType) {
            // Có licensePlate và subType
            page = subscriptionRepository.findByVehicleLicensePlateContainingIgnoreCaseAndDurationType(
                    trimmedLicensePlate, subType, sortedPageable);
        } else if (hasStatus && hasSubType) {
            // Có status và subType
            page = subscriptionRepository.findAllByStatusAndSubscriptionPricing_DurationType(
                    subStatus, subType, sortedPageable);
        } else if (hasLicensePlate) {
            // Chỉ có licensePlate - sử dụng LIKE
            page = subscriptionRepository.findByVehicleLicensePlateContainingIgnoreCase(
                    trimmedLicensePlate, sortedPageable);
        } else if (hasStatus) {
            // Chỉ có status
            page = subscriptionRepository.findAllByStatus(subStatus, sortedPageable);
        } else if (hasSubType) {
            // Chỉ có subType
            page = subscriptionRepository.findAllBySubscriptionPricing_DurationType(subType, sortedPageable);
        } else {
            // Không có filter nào, lấy tất cả
            page = subscriptionRepository.findAll(sortedPageable);
        }

        return page.map(subscriptionMapper::toResponse);
    }
}
