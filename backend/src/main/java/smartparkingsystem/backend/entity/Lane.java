package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

import lombok.NoArgsConstructor;
import smartparkingsystem.backend.entity.type.LaneTypeEnum;
import smartparkingsystem.backend.entity.type.LaneStatus;
@Entity(name = "lane")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lane {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lane_name", nullable = false, unique = true, length = 50)
    private  String laneName;

    @Column(name = "lane_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LaneTypeEnum laneType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LaneStatus status;

    @Column(name = "ip_camera", nullable = false, length = 100)
    private String ipCamera;
}
