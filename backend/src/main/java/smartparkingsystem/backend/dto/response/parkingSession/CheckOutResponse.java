package smartparkingsystem.backend.dto.response.parkingSession;

import lombok.Builder;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public class CheckOutResponse {
    private UUID id; //
    private String plateOutOcr; //
    private LocalDateTime timeOut; //
    private SessionStatus status; //
    private BigInteger fee; //
    private boolean isMonth; //
    private VehicleTypeEnum vehicleType; //
}
