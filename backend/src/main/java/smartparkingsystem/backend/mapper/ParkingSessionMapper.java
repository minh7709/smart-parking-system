package smartparkingsystem.backend.mapper;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.CheckInRequest;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.CheckOutRequest;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.ConfirmCheckInRequest;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.dto.response.parkingSession.ParkingSessionResponse;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.Subscription;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ParkingSessionMapper {
    public ParkingSession toEntityForConfirmCheckIn(ConfirmCheckInRequest request, Lane entryLane, boolean isMonth) {
        if (request == null || entryLane == null) {
            return null;
        }
        return ParkingSession.builder()
                .entryLane(entryLane)
                .vehicleType(request.getVehicleType())
                .finalPlate(request.getFinalPlate())
                .plateInOcr(request.getPlateInOcr())
                .status(SessionStatus.PARKED)
                .confidenceIn(request.getConfidenceIn())
                .month(isMonth)
                .timeIn(request.getTimeIn())
                .imageInUrl(request.getImageInUrl())
                .build();
    }

    public void updateEntityForCheckOut(ParkingSession session, Lane lane, String imageUrl, float confidenceOrRandom, String plateOutOcr, SessionStatus status, LocalDateTime timeOut) {
        if (session == null) {
            return;
        }
        session.setStatus(status);
        session.setTimeOut(timeOut);
        session.setExitLane(lane);
        session.setPlateOutOcr(plateOutOcr);
        session.setConfidenceOut(confidenceOrRandom);
        session.setImageOutUrl(imageUrl);
    }

    public CheckInResponse toCheckInResponse(String plateInOrc, String imageUrl, float confidenceIn, VehicleTypeEnum vehicleType) {
        if (plateInOrc == null || imageUrl == null || vehicleType == null || confidenceIn < 0 || confidenceIn > 1) {
            return null;
        }

        return CheckInResponse.builder()
                .plateInOcr(plateInOrc)
                .imageInUrl(imageUrl)
                .confidenceIn(confidenceIn)
                .vehicleType(vehicleType)
                .timeIn(LocalDateTime.now())
                .build();

    }

    public CheckOutResponse toCheckOutResponse(ParkingSession session, BigInteger parkingAmount, BigInteger penaltyAmount,
                                               List<UUID> relatedSessionIds, Lane lane, String imageUrl, Float confidenceOut, String plateOutOcr) {
        if (session == null) {
            return null;
        }

        return CheckOutResponse.builder()
                .id(session.getId())
                .plateOutOcr(session.getPlateOutOcr())
                .finalPlate(session.getFinalPlate())
                .timeIn(session.getTimeIn())
                .timeOut(session.getTimeOut())
                .status(session.getStatus())
                .parkingAmount(parkingAmount)
                .penaltyAmount(penaltyAmount)
                .isMonth(session.isMonth())
                .vehicleType(session.getVehicleType())
                .relatedSessionIds(relatedSessionIds)
                .exitLaneId(session.getExitLane().getId())
                .imageOutUrl(imageUrl)
                .confidenceOut(confidenceOut)
                .build();
    }
    public ParkingSessionResponse toParkingSessionResponse(ParkingSession session) {
        if (session == null) {
            return null;
        }

        return ParkingSessionResponse.builder()
                .id(session.getId())
                .entryLaneId(session.getEntryLane() != null ? session.getEntryLane().getId() : null)
                .exitLaneId(session.getExitLane() != null ? session.getExitLane().getId() : null)
                .vehicleType(session.getVehicleType())
                .timeIn(session.getTimeIn())
                .timeOut(session.getTimeOut())
                .plateInOcr(session.getPlateInOcr())
                .plateOutOcr(session.getPlateOutOcr())
                .finalPlate(session.getFinalPlate())
                .imageInUrl(session.getImageInUrl())
                .imageOutUrl(session.getImageOutUrl())
                .status(session.getStatus())
                .isMonth(session.isMonth())
                .build();
    }
}

