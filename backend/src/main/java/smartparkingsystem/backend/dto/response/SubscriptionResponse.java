package smartparkingsystem.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.SubStatus;
import smartparkingsystem.backend.entity.type.SubType;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID vehicleId;
    private SubType subType;
    private String licensePlate;
    private BigInteger price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubStatus subStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
