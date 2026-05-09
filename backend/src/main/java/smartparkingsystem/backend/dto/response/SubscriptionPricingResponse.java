package smartparkingsystem.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPricingResponse {
    private UUID id;
    private String pricingName;
    private VehicleTypeEnum vehicleType;
    private SubType durationType;
    private BigInteger price;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
    private Boolean active;
}
