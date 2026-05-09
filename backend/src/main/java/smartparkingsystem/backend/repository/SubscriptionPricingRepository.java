package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.SubscriptionPricing;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPricingRepository extends JpaRepository<SubscriptionPricing, UUID> {
    Page<SubscriptionPricing> findByVehicleType(VehicleTypeEnum vehicleType, Pageable pageable);
    Boolean existsByPricingName(String pricingName);
    Optional<SubscriptionPricing> findByVehicleTypeAndDurationTypeAndActiveTrue(VehicleTypeEnum vehicleType, SubType durationType);
}
