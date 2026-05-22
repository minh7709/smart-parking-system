package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.ConfirmCheckOutRequest;
import smartparkingsystem.backend.dto.response.admin.InvoiceResponse;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.InvoiceTypeEnum;
import smartparkingsystem.backend.entity.type.PaymentMethod;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.repository.InvoiceRepository;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    public Invoice createInvoiceForParkingSession(ParkingSession session, ConfirmCheckOutRequest request, User user) {

        Invoice invoice = new Invoice();
        invoice.setInvoiceType(InvoiceTypeEnum.PARKING_FEE); // Phân loại
        invoice.setParkingSession(session);
        invoice.setSubscription(null); // Đảm bảo sub_id = null

        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setParkingAmount(request.getParkingAmount());
        invoice.setPenaltyAmount(BigInteger.ZERO);
        invoice.setSubscriptionAmount(BigInteger.ZERO); // Bắt buộc = 0 theo CHECK Constraint
        invoice.setTotalAmount(request.getParkingAmount());

        invoice.setStatus(PaymentStatus.SUCCESS);
        invoice.setCashier(user);
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoiceStatus(Invoice invoice, PaymentStatus status) {
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoiceAmount(Invoice invoice, BigInteger parkingAmount, BigInteger penaltyAmount, BigInteger subscriptionAmount) {
        invoice.setSubscriptionAmount(subscriptionAmount);
        invoice.setParkingAmount(parkingAmount);
        invoice.setPenaltyAmount(penaltyAmount);
        invoice.setTotalAmount(parkingAmount.add(penaltyAmount));
        return invoiceRepository.save(invoice);
    }

    public Invoice createInvoiceForPenalty(ParkingSession session, BigInteger penaltyAmount, BigInteger parkingAmount, User user) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceType(InvoiceTypeEnum.PARKING_FEE);
        invoice.setParkingSession(session);
        invoice.setSubscription(null);

        invoice.setParkingAmount(parkingAmount);
        invoice.setPenaltyAmount(penaltyAmount);
        invoice.setSubscriptionAmount(BigInteger.ZERO); // Bắt buộc = 0
        invoice.setTotalAmount(parkingAmount.add(penaltyAmount));

        invoice.setStatus(PaymentStatus.SUCCESS);
        invoice.setCashier(user);
        return invoiceRepository.save(invoice);
    }

    public Invoice createInvoiceForSubscription(Subscription subscription, User user, PaymentMethod paymentMethod) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceType(InvoiceTypeEnum.SUBSCRIPTION_FEE); // Phân loại
        invoice.setSubscription(subscription);
        invoice.setParkingSession(null);

        invoice.setParkingAmount(BigInteger.ZERO);
        invoice.setPenaltyAmount(BigInteger.ZERO);
        invoice.setSubscriptionAmount(subscription.getPrice());
        invoice.setTotalAmount(subscription.getPrice());

        invoice.setPaymentTime(LocalDateTime.now());
        invoice.setStatus(PaymentStatus.PENDING);
        invoice.setCashier(user);
        invoice.setPaymentMethod(paymentMethod);
        return invoiceRepository.save(invoice);
    }

    /**
     * Lấy danh sách hóa đơn trong khoảng thời gian với phân trang
     */
    public Page<InvoiceResponse> getInvoices(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findByPaymentTimeBetween(startDate, endDate, pageable);
        return invoices.map(this::mapToResponse);
    }

    /**
     * Lấy danh sách hóa đơn trong khoảng thời gian với phân trang mặc định
     */
    public Page<InvoiceResponse> getInvoices(LocalDateTime startDate, LocalDateTime endDate) {
        // Sử dụng Pageable mặc định: trang 0, 10 phần tử
        return getInvoices(startDate, endDate, org.springframework.data.domain.PageRequest.of(0, 10));
    }

    /**
     * Helper: Chuyển đổi Invoice entity thành InvoiceResponse DTO
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceType(invoice.getInvoiceType())
                .parkingSessionId(invoice.getParkingSession() != null ? invoice.getParkingSession().getId() : null)
                .subscriptionId(invoice.getSubscription() != null ? invoice.getSubscription().getId() : null)
                .parkingAmount(invoice.getParkingAmount())
                .penaltyAmount(invoice.getPenaltyAmount())
                .subscriptionAmount(invoice.getSubscriptionAmount())
                .totalAmount(invoice.getTotalAmount())
                .cashierName(invoice.getCashier() != null ? invoice.getCashier().getUsername() : null)
                .paymentMethod(invoice.getPaymentMethod())
                .status(invoice.getStatus())
                .paymentTime(invoice.getPaymentTime())
                .build();
    }
}
