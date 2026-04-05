package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "parking_session")
@Getter
@Setter
public class ParkingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "entry_lane_id", nullable = false)
    private Lane entryLane;

    @ManyToOne
    @JoinColumn(name = "exit_lane_id")
    private Lane exitLane;

    @JoinColumn(name = "vehicle_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleTypeEnum vehicleType;

    @Column(name = "time_in", nullable = false)
    @CreationTimestamp
    private LocalDateTime timeIn;

    @Column(name = "time_out")
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

    @Column(name = "is_month")
    private boolean month;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
}

