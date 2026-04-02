package smartparkingsystem.backend.controller.v1.guard;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smartparkingsystem.backend.dto.request.parkingSessionRequest.*;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckInResponse;
import smartparkingsystem.backend.dto.response.parkingSession.CheckOutResponse;
import smartparkingsystem.backend.service.guard.ParkingSessionService;
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
//        HttpServletRequest httpRequest
//        // Logic kiểm tra IP từ Hotspot/4G như đã thảo luận
//        String clientIp = httpRequest.getRemoteAddr();
        CheckInResponse response = parkingSessionService.processCheckIn(request, image);
        return ResponseEntity.ok(ApiResponse.success(response, "Check-in successful"));
    }

    @PostMapping(value = "/check-out", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CheckOutResponse>> checkOut(
            @Valid @RequestPart CheckOutRequest request,
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

    @PostMapping("/report-incident/lost-card")
    public ResponseEntity<ApiResponse<CheckOutResponse>> reportLostCard(
            @Valid @RequestBody CheckOutWithoutCardRequest request,
            @RequestPart("image") MultipartFile image) {
        CheckOutResponse response = parkingSessionService.reportLostCard(request, image);
        return ResponseEntity.ok(ApiResponse.success(response, "Incident reported successfully"));

    }
}
