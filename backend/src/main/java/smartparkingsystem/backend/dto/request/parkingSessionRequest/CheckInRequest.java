package smartparkingsystem.backend.dto.request.parkingSessionRequest;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Validated
public class CheckInRequest {
    @NotNull(message = "Entry lane ID is required")
    private UUID entryLaneId;

    @NotNull(message = "Vehicle type is required")
    private VehicleTypeEnum vehicleType;
}
/*
@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "entry_lane_id", nullable = false)
    private Lane entryLane;

    @ManyToOne
    @JoinColumn(name = "exit_lane_id")
    private Lane exitLane;

    @Column(name = "time_in", nullable = false)
    @Timestamp
    private LocalDateTime timeIn;

    @Column(name = "time_out")
    @Timestamp
    private LocalDateTime timeOut;

    @Column(name = "plate_in_ocr" , nullable = false, length = 20)
    private String plateInOcr;

    @Column(name = "plate_out_ocr", length = 20)
    private String plateOutOcr;

    @Column(name = "final_plate", length = 20)
    private String finalPlate;

    @Column(name = "image_in_url")
    private String imageInUrl;

    @Column(name = "image_out_url")
    private String imageOutUrl;

    @Column(name = "confidence_in")
    private Float confidenceIn;

    @Column(name = "confidence_out")
    private Float confidenceOut;

    @Column(name = "is_month", nullable = false)
    private boolean month;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
*/