package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
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

    @Column(name = "amount", nullable = false)
    private BigInteger amount;

    @Column(name = "penalty_amount")
    private BigInteger penaltyAmount;

    @OneToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(name = "payment_time", nullable = false)
    @UpdateTimestamp
    private LocalDateTime paymentTime;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_ref", length = 50)
    private String transactionRef;

    @Column(name = "status", nullable = false)
    private PaymentStatus status;
}
