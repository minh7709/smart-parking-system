package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {
    boolean existsByRuleName(String ruleName);
    Optional<PricingRule> findByVehicleTypeAndActiveTrue(VehicleTypeEnum vehicleType);
    Page<PricingRule> findByVehicleType(VehicleTypeEnum vehicleType, Pageable pageable);
}
