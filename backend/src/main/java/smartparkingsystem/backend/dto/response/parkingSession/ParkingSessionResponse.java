package smartparkingsystem.backend.dto.response.parkingSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSessionResponse {
    private UUID id; //
    private Lane entryLane;
    private Lane exitLane;

    private String plateInOcr;
    private String plateOutOcr; //
    private String finalPlate;

    private LocalDateTime timeIn;
    private LocalDateTime timeOut; //

    private SessionStatus status; //
    private BigInteger fee; //
    private boolean isMonth; //
    private VehicleTypeEnum vehicleType; //
}
