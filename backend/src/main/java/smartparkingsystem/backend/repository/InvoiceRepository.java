package smartparkingsystem.backend.repository;

import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smartparkingsystem.backend.entity.Invoice;
import smartparkingsystem.backend.entity.ParkingSession;
import tools.jackson.databind.ext.OptionalHandlerFactory;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByParkingSessionId(UUID parkingSessionId);
    Optional<Invoice> findById(UUID id);
    Optional<Invoice> findByParkingSession(ParkingSession parkingSession);
    Optional<Invoice> findBySubscriptionId(UUID subscriptionId);
}
