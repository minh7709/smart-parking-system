package smartparkingsystem.backend.controller.v1.guard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.request.VehicleRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.VehicleReponse;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.service.guard.VehicleService;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('GUARD', 'ADMIN')")
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<VehicleReponse>>> getVehicles(
            @RequestParam(required = false) VehicleTypeEnum vehicleType,
            Pageable pageable) {
        Page<VehicleReponse> response = vehicleService.getVehicles(pageable, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicles fetched successfully"));

    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<VehicleReponse>> createVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleReponse response = vehicleService.createVehicle(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleReponse>> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        VehicleReponse response = vehicleService.updateVehicle(request, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleReponse>> deleteVehicle(@PathVariable UUID id) {
        VehicleReponse response = vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleReponse>> getVehicleById(@PathVariable UUID id) {
        VehicleReponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle retrieved successfully"));
    }

    @GetMapping("/license-plate/{licensePlate}")
    public ResponseEntity<ApiResponse<VehicleReponse>> getVehicleByLicensePlate(@PathVariable String licensePlate) {
        VehicleReponse response = vehicleService.getVehicleByLicensePlate(licensePlate);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicle retrieved successfully"));
    }
}