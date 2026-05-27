package smartparkingsystem.backend.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartparkingsystem.backend.dto.request.PricingRuleRequest;
import smartparkingsystem.backend.entity.PricingRule;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.exception.InvalidStateException;
import smartparkingsystem.backend.exception.ValidationException;
import smartparkingsystem.backend.mapper.PricingRuleMapper;
import smartparkingsystem.backend.repository.PricingRuleRepository;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.validation.pricingRule.PricingValidatorFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingRuleServiceTest {
    @Mock
    private PricingRuleMapper pricingRuleMapper;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @Mock
    private UserService userService;

    @Mock
    private PricingValidatorFactory pricingValidatorFactory;

    @InjectMocks
    private PricingRuleService pricingRuleService;

    @Test
    void updatePricingRule_changeVehicleType_throwException() {
        UUID id = UUID.randomUUID();
        PricingRule existing = new PricingRule();
        existing.setVehicleType(VehicleTypeEnum.CAR);
        existing.setRuleName("Rule A");

        PricingRuleRequest request = PricingRuleRequest.builder()
                .ruleName("Rule A")
                .vehicleType(VehicleTypeEnum.MOTOR)
                .pricingStrategy(PricingStrategyEnum.FLAT_RATE)
                .penaltyFee(BigInteger.TEN)
                .isActive(false)
                .build();

        when(pricingRuleRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ValidationException.class, () -> pricingRuleService.updatePricingRule(id, request));
    }

    @Test
    void activatePricingRule_alreadyActive_throwException() {
        UUID id = UUID.randomUUID();
        PricingRule rule = new PricingRule();
        rule.setActive(true);

        when(pricingRuleRepository.findById(id)).thenReturn(Optional.of(rule));

        assertThrows(InvalidStateException.class, () -> pricingRuleService.activatePricingRule(id));
    }
}
