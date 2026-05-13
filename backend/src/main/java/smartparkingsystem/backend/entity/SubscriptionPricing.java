package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import smartparkingsystem.backend.entity.type.SubType;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_pricing")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pricing_name", nullable = false, unique = true, length = 100)
    private String pricingName;

    @Column(name = "vehicle_type", nullable = false, columnDefinition = "vehicle_type_enum")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private VehicleTypeEnum vehicleType;

    @Column(name = "duration_type", nullable = false, columnDefinition = "sub_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SubType durationType;

    @Column(name = "price", nullable = false)
    private BigInteger price;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User creator;
}
