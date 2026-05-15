package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public Invoice createInvoiceForParkingSession(ParkingSession session, BigInteger parking_amount, User user) {

        Invoice invoice = new Invoice();
        invoice.setInvoiceType(InvoiceTypeEnum.PARKING_FEE); // Phân loại
        invoice.setParkingSession(session);
        invoice.setSubscription(null); // Đảm bảo sub_id = null

        invoice.setParkingAmount(parking_amount);
        invoice.setPenaltyAmount(BigInteger.ZERO);
        invoice.setSubscriptionAmount(BigInteger.ZERO); // Bắt buộc = 0 theo CHECK Constraint
        invoice.setTotalAmount(parking_amount);

        invoice.setStatus(PaymentStatus.PENDING);
        invoice.setCashier(user);
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoiceStatus(Invoice invoice, PaymentStatus status, PaymentMethod paymentMethod) {
        invoice.setPaymentTime(LocalDateTime.now());
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoiceStatus(Invoice invoice, PaymentStatus status) {
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoiceAmount(Invoice invoice, BigInteger parkingAmount, BigInteger penaltyAmount) {
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
}
