package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void cancelSubscription (UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói đăng ký"));
        if(subscription.getStatus() != SubStatus.PENDING && subscription.getStatus() != SubStatus.ACTIVE){
            throw new InvalidStateException("Không thể thay đổi trạng thái của gói đăng ký có trạng thái hiện tại: " + subscription.getStatus());
        }
        Invoice invoice = invoiceRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("không tìm thấy hóa đơn của gói đăng ký này"));

        subscription.setStatus(SubStatus.CANCELLED);
        invoice.setStatus(PaymentStatus.FAILED);
        subscriptionRepository.save(subscription);
        invoiceRepository.save(invoice);
    }

}
