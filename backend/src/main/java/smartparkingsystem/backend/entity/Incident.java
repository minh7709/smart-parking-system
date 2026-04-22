package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import smartparkingsystem.backend.entity.type.IncidentTypeEnum;
@Data
@Entity
@Table(name = "incident")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ParkingSession parkingSession;

    @ManyToOne
    @JoinColumn(name = "reported_by", nullable = false)
    private User reporter;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "reported_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime reportedAt;

    @Column(name = "incident_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private IncidentTypeEnum incidentType;

    @Column(name = "evidence_url")
    private String evidenceUrl;
}
