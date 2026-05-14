package smartparkingsystem.backend.dto.response.parkingSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private String plateInOcr; // Biển số cuối cùng được xác nhận
    private String imageInUrl; // URL hình ảnh lúc check-in
    private LocalDateTime timeIn; // Thời gian check-in
    private Float confidenceIn;
    private VehicleTypeEnum vehicleType; //
}
