package smartparkingsystem.backend.service.guard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.repository.InvoiceRepository;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoiceForParkingSession_shouldSumAmounts() {
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice invoice = invoiceService.createInvoiceForParkingSession(
                new ParkingSession(),
                BigInteger.valueOf(1000),
                BigInteger.valueOf(200),
                null,
                null,
                PaymentStatus.SUCCESS,
                java.time.LocalDateTime.now()
        );

        assertEquals(BigInteger.valueOf(1200), invoice.getTotalAmount());
    }
}
