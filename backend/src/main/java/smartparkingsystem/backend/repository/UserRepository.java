package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.User;

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
}

