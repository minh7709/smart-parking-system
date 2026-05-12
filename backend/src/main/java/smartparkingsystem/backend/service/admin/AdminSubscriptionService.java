package smartparkingsystem.backend.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public void confirmSubscription (UUID subscriptionId, SubStatus newStatus) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));
        if(subscription.getStatus() != SubStatus.PENDING && subscription.getStatus() != SubStatus.ACTIVE){
            throw new InvalidStateException("Only pending or active subscriptions can be confirmed or rejected");
        }
        Invoice invoice = invoiceRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for subscription id: " + subscriptionId));

        if(newStatus == SubStatus.ACTIVE){
            subscription.setStatus(SubStatus.ACTIVE);
            invoice.setStatus(PaymentStatus.SUCCESS);
        } else if(newStatus == SubStatus.CANCELLED){
            subscription.setStatus(SubStatus.CANCELLED);
            invoice.setStatus(PaymentStatus.FAILED);
        } else {
            throw new ValidationException("Invalid status: " + newStatus + ". Only ACTIVE or REJECTED are allowed");
        }
        subscriptionRepository.save(subscription);
    }

}
