package smartparkingsystem.backend.controller.v1.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartparkingsystem.backend.dto.response.ApiResponse;
import smartparkingsystem.backend.dto.response.admin.*;
import smartparkingsystem.backend.entity.type.VehicleTypeEnum;
import smartparkingsystem.backend.service.admin.StatisticsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SummaryResponse summary = statisticsService.getSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Thống kê tổng quan thành công"));
    }
    @GetMapping("/traffic/timeline")
    public ResponseEntity<ApiResponse<List<TrafficTimelineResponse>>> getTrafficTimeline(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam String interval,
            @RequestParam(required = false) VehicleTypeEnum vehicleType) {
        List<TrafficTimelineResponse> timeline = statisticsService.getTrafficTimeline(startDate, endDate, interval, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(timeline, "Lấy lưu lượng xe thành công"));
    }

    @GetMapping("/traffic/lanes")
    public ResponseEntity<ApiResponse<List<LaneUtilizationResponse>>> getLaneUtilization(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LaneUtilizationResponse> laneUtilization = statisticsService.getLaneUtilization(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(laneUtilization, "Lấy tình trạng làn xe thành công"));
    }

    @GetMapping("/revenue/timeline")
    public ResponseEntity<ApiResponse<List<RevenueTimelineResponse>>> getRevenueTimeline(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam String interval) {
        List<RevenueTimelineResponse> timeline = statisticsService.getRevenueTimeline(startDate, endDate, interval);
        return ResponseEntity.ok(ApiResponse.success(timeline, "Lấy doanh thu theo thời gian thành công"));
    }

    @GetMapping("/revenue/breakdown")
    public ResponseEntity<ApiResponse<RevenueBreakdownResponse>> getRevenueBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        RevenueBreakdownResponse breakdown = statisticsService.getRevenueBreakdown(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(breakdown, "Lấy doanh thu theo loại xe thành công"));
    }

    @GetMapping("/revenue/penalties")
    public ResponseEntity<ApiResponse<PenaltyFeesResponse>> getPenaltyFees(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        PenaltyFeesResponse penalties = statisticsService.getTotalPenalties(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(penalties, "Lấy phí phạt thành công"));
    }
}