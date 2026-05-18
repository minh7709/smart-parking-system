package smartparkingsystem.backend.controller.v1.guard;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.service.guard.ParkingSessionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

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

    @DeleteMapping("/cancel-check-in")
    public ResponseEntity<ApiResponse<Void>> cancelCheckIn(@Valid @RequestBody String imageUrl) {
        parkingSessionService.cancelCheckIn(imageUrl);
        return ResponseEntity.ok(ApiResponse.success(null, "Check-in cancelled successfully"));
    }

    @PostMapping(value = "/check-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CheckOutResponse>> checkOut(
            @Valid @RequestPart("request") CheckOutRequest request,
            @RequestPart("image") MultipartFile image) {
        CheckOutResponse response = parkingSessionService.processCheckOut(request, image);
        return ResponseEntity.ok(ApiResponse.success(response, "Check-out successful"));
    }

    @PostMapping("/confirm-check-in")
    public ResponseEntity<ApiResponse<ParkingSessionResponse>> confirmCheckIn(
            @Valid @RequestBody ConfirmCheckInRequest request) {
        ParkingSessionResponse parkingSessionResponse = parkingSessionService.processConfirmCheckIn(request);
        return ResponseEntity.ok(ApiResponse.success(parkingSessionResponse, "Check-in confirmed successfully"));
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

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ParkingSessionResponse>>> getParkingSessionsWithFilters(
            @RequestParam (required = false) String licensePlate,
            @RequestParam (required = false) VehicleTypeEnum vehicleType,
            @RequestParam (required = false) SessionStatus status,
            Pageable pageable) {
        Page<ParkingSessionResponse> page = parkingSessionService.getAllParkingSessions(pageable, status, licensePlate, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(page, "Parking sessions retrieved successfully"));
    }
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalParkedVehicles(
            @RequestParam (required = false) SessionStatus status
    ) {
        Long total = parkingSessionService.getTotalParkedVehicles(status);
        return ResponseEntity.ok(ApiResponse.success(total, "Total parked vehicles retrieved successfully"));
    }

    @GetMapping("/{parkingSessionId}/image")
    public ResponseEntity<Resource> getParkingSessionImage(
            @PathVariable UUID parkingSessionId,
            @RequestParam String type) {
        Path imagePath = parkingSessionService.getParkingSessionImagePath(parkingSessionId, type);
        String contentType = "application/octet-stream";
        try {
            String detectedType = Files.probeContentType(imagePath);
            if (detectedType != null && !detectedType.isBlank()) {
                contentType = detectedType;
            }
        } catch (IOException ignored) {
        }

        FileSystemResource resource = new FileSystemResource(imagePath.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imagePath.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
