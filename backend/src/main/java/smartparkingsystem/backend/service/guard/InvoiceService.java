package smartparkingsystem.backend.service.guard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.User;
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
        invoice.setParkingSession(session);
        invoice.setParkingAmount(parking_amount);
        invoice.setPenaltyAmount(BigInteger.ZERO);
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

    public Invoice createInvoiceForPenalty(ParkingSession session, BigInteger penaltyAmount, BigInteger parkingAmount, User user) {
        Invoice invoice = new Invoice();
        invoice.setParkingSession(session);
        invoice.setPenaltyAmount(penaltyAmount);
        invoice.setParkingAmount(parkingAmount);
        invoice.setTotalAmount(penaltyAmount.add(parkingAmount));
        invoice.setStatus(PaymentStatus.SUCCESS);
        invoice.setCashier(user);
        return invoiceRepository.save(invoice);
    }
}
