package smartparkingsystem.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String userName);
    boolean existsByUsername(String userName);
    Optional<User> findByPhone(String phone);
}

