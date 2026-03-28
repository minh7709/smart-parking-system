package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.SubStatus;
import java.math.BigInteger;
import java.time.LocalDateTime;
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

    @Column(name = "price", nullable = false)
    private BigInteger price;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubType type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubStatus status;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
