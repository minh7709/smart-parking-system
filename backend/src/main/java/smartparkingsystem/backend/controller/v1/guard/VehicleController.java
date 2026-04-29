package smartparkingsystem.backend.controller.v1.guard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.VehicleReponse;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.service.auth.UserService;
import smartparkingsystem.backend.service.guard.VehicleService;

@RestController
@RequestMapping("/api/v1/guard/vehicles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('GUARD')")
public class VehicleController {
    private final VehicleService vehicleService;
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Page<VehicleReponse>>> getVehicles(
            @RequestParam(required = false) VehicleTypeEnum vehicleType,
            Pageable pageable) {
        Page<VehicleReponse> response = vehicleService.getVehicles(pageable, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(response, "Vehicles fetched successfully"));

    }
}