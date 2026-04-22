package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import smartparkingsystem.backend.entity.type.PaymentMethod;
import smartparkingsystem.backend.entity.type.PaymentStatus;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "session_id")
    private ParkingSession parkingSession;

    @OneToOne
    @JoinColumn(name = "sub_id")
    private Subscription subscription;

    @Column(name = "parking_amount")
    private BigInteger parkingAmount;

    @Column(name = "penalty_amount")
    private BigInteger penaltyAmount;

    @Column(name = "total_amount", nullable = false)
    private BigInteger totalAmount;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(name = "payment_time")
    @UpdateTimestamp
    private LocalDateTime paymentTime;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_ref", length = 50)
    private String transactionRef;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentStatus status;
}
