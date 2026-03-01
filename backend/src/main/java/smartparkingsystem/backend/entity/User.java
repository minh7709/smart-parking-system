package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    String userName;

    @Column(name = "password", nullable = false, unique = true)
    String password;

    @Column(name = "fullname", nullable = false, unique = true, length = 100)
    String fullName;

    @Column(name = "phone", nullable = false, unique = true, length = 11)
    String phone;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    UserRole role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    UserStatus status;

    @Column(name = "created_at", nullable = false)
    @Timestamp
    LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    boolean isDeleted;
}
