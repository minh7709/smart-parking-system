package smartparkingsystem.backend.controller.v1.guard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.LaneResponse;
import smartparkingsystem.backend.service.guard.LaneService;

import java.util.List;


@RestController
@RequestMapping("/api/v1/guard/active-lanes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('GUARD')")
public class LaneController {
    private final LaneService laneService;
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<LaneResponse>>> getActiveLanes() {
        List<LaneResponse> lanes = laneService.getActiveLanes();
        return ResponseEntity.ok(ApiResponse.success(lanes, "Active lanes retrieved successfully"));
    }
}
