package smartparkingsystem.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleReponse {
    private UUID id;
    private String licensePlate;
    private VehicleTypeEnum vehicleType;
    private String brand;
    private String customerName;
    private String customerPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
