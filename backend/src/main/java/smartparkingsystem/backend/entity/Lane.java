package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;
import smartparkingsystem.backend.entity.type.LaneStatus;
@Entity(name = "lane")
@Data
public class Lane {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lane_name", nullable = false, unique = true, length = 50)
    private  String laneName;

    @Column(name = "secret_key", nullable = false, unique = true, length = 255)
    private  String secretKey;

    @Column(name = "lane_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LaneTypeEnum laneType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LaneStatus status;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "ip_camera", nullable = false, length = 100)
    private String ipCamera;
}
