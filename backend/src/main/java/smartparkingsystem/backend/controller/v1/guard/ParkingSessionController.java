package smartparkingsystem.backend.controller.v1.guard;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.dto.request.IncidentRequest;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.*;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.dto.response.parkingSession.ParkingSessionResponse;
import smartparkingsystem.backend.entity.type.SessionStatus;
import smartparkingsystem.backend.service.guard.ParkingSessionService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/guard/parking-session")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'GUARD')")
public class ParkingSessionController {
    private final ParkingSessionService parkingSessionService;

    @PostMapping(value = "/check-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @RequestPart("request") @Valid CheckInRequest request,
            @RequestPart("image") MultipartFile image) {
        CheckInResponse response = parkingSessionService.processCheckIn(request, image);
        return ResponseEntity.ok(ApiResponse.success(response, "Check-in successful"));
    }

    @PostMapping(value = "/check-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CheckOutResponse>> checkOut(
            @Valid @RequestPart("request") CheckOutRequest request,
            @RequestPart("image") MultipartFile image) {
        CheckOutResponse response = parkingSessionService.processCheckOut(request, image);
        return ResponseEntity.ok(ApiResponse.success(response, "Check-out successful"));
    }

    @PostMapping("/confirm-check-in")
    public ResponseEntity<ApiResponse<CheckInResponse>> confirmCheckIn(
            @Valid @RequestBody ConfirmCheckInRequest request) {
        CheckInResponse checkInResponse = parkingSessionService.processConfirmCheckIn(request);
        return ResponseEntity.ok(ApiResponse.success(checkInResponse, "Check-in confirmed successfully"));
    }

    @PostMapping("/confirm-check-out")
    public ResponseEntity<ApiResponse<Void>> confirmCheckOut(
            @Valid @RequestBody ConfirmCheckOutRequest request) {
        parkingSessionService.processConfirmCheckOut(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Check-out confirmed successfully"));
    }

    @PostMapping(value = "/report-incident/lost-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CheckOutResponse>> reportLostCard(
            @Valid @RequestPart("request") CheckOutWithoutCardRequest request,
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "evidenceImage") MultipartFile evidenceImage) {
        CheckOutResponse response = parkingSessionService.reportLostCard(request, image, evidenceImage);
        return ResponseEntity.ok(ApiResponse.success(response, "Incident reported successfully"));

    }

    @PostMapping(value = "/report-incident", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> reportGeneralIncident(
            @Valid @RequestPart("request") IncidentRequest request,
            @RequestPart(value = "evidenceImage") MultipartFile evidenceImage) {
        parkingSessionService.reportGeneralIncident(request, evidenceImage);
        return ResponseEntity.ok(ApiResponse.success(null, "Incident reported successfully"));
    }

    @GetMapping("/{plate}")
    public ResponseEntity<ApiResponse<List<ParkingSessionResponse>>> getParkingSessionsByPlate(
            @PathVariable @Valid String plate,
            @RequestParam(required = false) SessionStatus sessionStatus) {
        List<ParkingSessionResponse> response = parkingSessionService.getParkingSessionsByPlate(plate, sessionStatus);
        return ResponseEntity.ok(ApiResponse.success(response, "Parking sessions retrieved successfully"));
    }
    @GetMapping("/parked")
    public ResponseEntity<ApiResponse<Page<ParkingSessionResponse>>> getAllParkingSessions(
            Pageable pageable) {
        Page<ParkingSessionResponse> page = parkingSessionService.getAllParkingSessions(pageable, SessionStatus.PARKED);
        return ResponseEntity.ok(ApiResponse.success(page, "Parking sessions retrieved successfully"));
    }
}
