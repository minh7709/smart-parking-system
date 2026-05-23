package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.type.PaymentStatus;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ResourceNotFoundException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.repository.InvoiceRepository;
import smartparkingsystem.backend.repository.ParkingSessionRepository;
import smartparkingsystem.backend.repository.SubscriptionRepository;
import smartparkingsystem.backend.service.guard.ParkingSessionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final ParkingSessionService parkingSessionService;

    @Transactional
    public void cancelSubscription (UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói đăng ký"));
        if(subscription.getStatus() != SubStatus.PENDING && subscription.getStatus() != SubStatus.ACTIVE){
            throw new InvalidStateException("Không thể thay đổi trạng thái của gói đăng ký có trạng thái hiện tại: " + subscription.getStatus());
        }
        Invoice invoice = invoiceRepository.findBySubscription(subscription)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn cho gói đăng ký này"));
        invoice.setStatus(PaymentStatus.FAILED);
        subscription.setStatus(SubStatus.CANCELLED);
        subscriptionRepository.save(subscription);
        invoiceRepository.save(invoice);
        parkingSessionService.handleSubscriptionChanging(subscription, SubStatus.CANCELLED);
    }

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findAllByStatusAndEndDateBefore(SubStatus.ACTIVE, now);
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubStatus.EXPIRED);
            parkingSessionService.handleSubscriptionChanging(subscription, SubStatus.EXPIRED);
        }
        subscriptionRepository.saveAll(expiredSubscriptions);
    }
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processPendingSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> pendingSubscriptions = subscriptionRepository.findAllByStatusAndStartDateBefore(SubStatus.PENDING, now);
        for (Subscription subscription : pendingSubscriptions) {
            Invoice invoice = invoiceRepository.findBySubscription(subscription)
                    .orElse(null);
            if (invoice == null) {
                continue;
            }
            invoice.setStatus(PaymentStatus.SUCCESS);
            invoiceRepository.save(invoice);

            subscription.setStatus(SubStatus.ACTIVE);
            parkingSessionService.handleSubscriptionChanging(subscription, SubStatus.ACTIVE);
        }
        subscriptionRepository.saveAll(pendingSubscriptions);
    }

}
