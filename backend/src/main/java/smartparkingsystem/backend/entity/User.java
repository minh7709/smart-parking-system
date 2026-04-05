package smartparkingsystem.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
    String username;

    @Column(name = "password", nullable = false, unique = true)
    String password;

    @Column(name = "full_name", nullable = false, unique = true, length = 100)
    String fullName;

    @Column(name = "phone", nullable = false, unique = true, length = 11)
    String phone;

    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    UserRole role;

    @Column(name = "status", nullable = false, columnDefinition = "user_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    UserStatus status;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    boolean isDeleted;
}
