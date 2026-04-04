package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import smartparkingsystem.backend.entity.type.PricingStrategyEnum;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.config.TimeWindowAndProgressiveConfig;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pricing_rule")
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_name", nullable = false, unique = true, length = 100)
    private String ruleName;

    @Column(name = "vehicle_type", nullable = false, columnDefinition = "vehicle_type")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private VehicleTypeEnum vehicleType;

    @Column(name = "strategy", nullable = false, columnDefinition = "pricing_strategy_enum")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PricingStrategyEnum strategy;

    @Column(name = "base_price", nullable = false)
    private BigInteger basePrice;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "block_minutes")
    private Integer blockMinutes;

    @Column(name = "threshold_minutes")
    private Integer thresholdMinutes;

    @Column(name = "threshold_price")
    private BigInteger thresholdPrice;

    @Column(name = "max_price_per_day")
    private BigInteger maxPricePerDay;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "progressive_config", columnDefinition = "jsonb")
    private List<TimeWindowAndProgressiveConfig> progressiveConfig;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "penalty_fee", nullable = false, columnDefinition = "bigint default 0")
    private BigInteger penaltyFee;

    @Column(name = "created_at", nullable = false)
    @Timestamp
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User creator;
}
