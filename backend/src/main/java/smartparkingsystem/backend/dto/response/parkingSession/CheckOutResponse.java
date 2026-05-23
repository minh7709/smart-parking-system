package smartparkingsystem.backend.dto.response.parkingSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutResponse {
    private UUID id; //
    private String plateOutOcr; //
    private String finalPlate;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut; //
    private SessionStatus status; //
    private BigInteger parkingAmount; //
    private BigInteger penaltyAmount; //
    private String imageOutUrl; //
    private Float confidenceOut; //
    private UUID exitLaneId; //
    private boolean isMonth; //
    private VehicleTypeEnum vehicleType; //
    private List<UUID> relatedSessionIds;
}
