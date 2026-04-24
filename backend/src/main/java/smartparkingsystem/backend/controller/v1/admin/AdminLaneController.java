package smartparkingsystem.backend.controller.v1.admin;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smartparkingsystem.backend.dto.request.LaneCreateRequest;
import smartparkingsystem.backend.dto.request.LaneUpdateRequest;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.LaneResponse;
import smartparkingsystem.backend.service.admin.AdminLaneService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/lanes")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminLaneController {
	private final AdminLaneService adminLaneService;

	@PostMapping
	public ResponseEntity<ApiResponse<LaneResponse>> createLane(@Valid @RequestBody LaneCreateRequest request) {
		LaneResponse response = adminLaneService.createLane(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response, "Lane created successfully"));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<LaneResponse>>> getAllLanes() {
		List<LaneResponse> response = adminLaneService.getAllLanes();
		return ResponseEntity.ok(ApiResponse.success(response, "Lanes fetched successfully"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<LaneResponse>> getLaneById(@PathVariable UUID id) {
		LaneResponse response = adminLaneService.getLaneById(id);
		return ResponseEntity.ok(ApiResponse.success(response, "Lane fetched successfully"));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<LaneResponse>> updateLane(
			@PathVariable UUID id,
			@Valid @RequestBody LaneUpdateRequest request) {
		LaneResponse response = adminLaneService.updateLane(id, request);
		return ResponseEntity.ok(ApiResponse.success(response, "Lane updated successfully"));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteLane(@PathVariable UUID id) {
		adminLaneService.deleteLane(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Lane deleted successfully"));
	}
}
