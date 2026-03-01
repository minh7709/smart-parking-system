package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Data;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.entity.type.SubStatus;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Data
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "type", nullable = false)
    private VehicleTypeEnum vehicleType;

    @Column(name = "price", nullable = false)
    private BigInteger price;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private SubStatus status;

    @Column(name = "created_at", nullable = false)
    @Timestamp
    private LocalDate createdAt;
}
