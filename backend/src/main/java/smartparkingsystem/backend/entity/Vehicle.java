package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Data;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicle")
@Data
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "vehicle_type", nullable = false, length = 20)
        @Enumerated(EnumType.STRING)
    private VehicleTypeEnum vehicleType;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 11)
    private String customerPhone;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "created_at", nullable = false)
    @Timestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Timestamp
    private LocalDateTime updatedAt;

}
