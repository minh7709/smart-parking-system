package smartparkingsystem.backend.mapper;

import org.springframework.stereotype.Component;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.CheckInRequest;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.entity.Lane;
import smartparkingsystem.backend.entity.ParkingSession;
import smartparkingsystem.backend.entity.type.SessionStatus;

import java.math.BigInteger;

@Component
public class ParkingSessionMapper {

    public ParkingSession toEntityForCheckIn(CheckInRequest request, Lane entryLane, String plateInOcr, float confidenceIn) {
        if (request == null || entryLane == null) {
            return null;
        }

        return ParkingSession.builder()
                .entryLane(entryLane)
                .vehicleType(request.getVehicleType())
                .plateInOcr(plateInOcr)
                .status(SessionStatus.PARKED)
                .month(false)
                .confidenceIn(confidenceIn)
                .build();
    }

    public CheckInResponse toCheckInResponse(ParkingSession session) {
        if (session == null) {
            return null;
        }

        return CheckInResponse.builder()
                .id(session.getId())
                .plateInOcr(session.getPlateInOcr())
                .finalPlate(session.getFinalPlate())
                .timeIn(session.getTimeIn())
                .status(session.getStatus())
                .isMonth(session.isMonth())
                .vehicleType(session.getVehicleType())
                .build();
    }

    public CheckOutResponse toCheckOutResponse(ParkingSession session, BigInteger fee) {
        if (session == null) {
            return null;
        }

        return CheckOutResponse.builder()
                .id(session.getId())
                .plateOutOcr(session.getPlateOutOcr())
                .finalPlate(session.getFinalPlate())
                .timeOut(session.getTimeOut())
                .status(session.getStatus())
                .fee(fee)
                .isMonth(session.isMonth())
                .vehicleType(session.getVehicleType())
                .build();
    }
}

