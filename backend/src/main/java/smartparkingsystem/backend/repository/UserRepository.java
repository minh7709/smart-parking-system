package smartparkingsystem.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String userName);
    boolean existsByUsername(String userName);
    boolean existsByPhone(String phone);
    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByPhoneAndIdNot(String phone, UUID id);
    Optional<User> findByPhone(String phone);

    Optional<User> findByUsernameAndDeletedFalse(String username);
    Optional<User> findByIdAndDeletedFalse(UUID id);
    List<User> findAllByDeletedFalse();
    boolean existsByUsernameAndDeletedFalse(String username);
    boolean existsByPhoneAndDeletedFalse(String phone);
    boolean existsByUsernameAndDeletedFalseAndIdNot(String username, UUID id);
    boolean existsByPhoneAndDeletedFalseAndIdNot(String phone, UUID id);

    // Partial phone search methods (LIKE pattern) - deleted = false
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.phone LIKE CONCAT('%', :phone, '%')")
    List<User> findByPhoneContainingAndDeletedFalse(@Param("phone") String phone);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.status = :status")
    List<User> findByStatusAndDeletedFalse(@Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.phone LIKE CONCAT('%', :phone, '%') AND u.status = :status")
    List<User> findByPhoneContainingAndStatusAndDeletedFalse(@Param("phone") String phone, @Param("status") UserStatus status);

    // Pageable versions for future use
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.phone LIKE CONCAT('%', :phone, '%')")
    Page<User> findByPhoneContainingAndDeletedFalse(@Param("phone") String phone, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.status = :status")
    Page<User> findByStatusAndDeletedFalse(@Param("status") UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.phone LIKE CONCAT('%', :phone, '%') AND u.status = :status")
    Page<User> findByPhoneContainingAndStatusAndDeletedFalse(@Param("phone") String phone, @Param("status") UserStatus status, Pageable pageable);
}

