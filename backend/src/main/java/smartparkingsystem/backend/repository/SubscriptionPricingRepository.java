package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.SubscriptionPricing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPricingRepository extends JpaRepository<SubscriptionPricing, UUID> {
    Optional<SubscriptionPricing> findBySubscriptionId(UUID subscriptionId);
    Optional<List<SubscriptionPricing>> findAllBySubscriptionId();
}
