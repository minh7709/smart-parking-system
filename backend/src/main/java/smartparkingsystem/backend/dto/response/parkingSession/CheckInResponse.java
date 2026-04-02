package smartparkingsystem.backend.dto.response.parkingSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private UUID id;
    private String plateInOcr; // Biển số cuối cùng được xác nhận
    private String finalPlate; // Biển số cuối cùng được xác nhận
    private LocalDateTime timeIn;
    private SessionStatus status;
    private boolean isMonth; //
    private VehicleTypeEnum vehicleType; //
}
