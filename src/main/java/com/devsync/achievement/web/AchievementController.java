package com.devsync.achievement.web;

import com.devsync.achievement.dto.CreateAchievementRequest;
import com.devsync.achievement.dto.AchievementResponse;
import com.devsync.achievement.dto.UpdateAchievementRequest;
import com.devsync.achievement.entity.AchievementType;
import com.devsync.achievement.service.AchievementService;
import com.devsync.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/achievements")
@Tag(name = "Achievement Management", description = "Endpoints for unlocking, updating, and querying user badges, milestones, and achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @PostMapping
    @Operation(summary = "Create an achievement", description = "Unlock/award an achievement to a user")
    public ResponseEntity<ApiResponse<AchievementResponse>> createAchievement(@Valid @RequestBody CreateAchievementRequest request) {
        AchievementResponse response = achievementService.createAchievement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Achievement created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get achievement by ID", description = "Retrieve achievement details by UUID")
    public ResponseEntity<ApiResponse<AchievementResponse>> getAchievementById(@PathVariable UUID id) {
        AchievementResponse response = achievementService.getAchievementById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an achievement", description = "Update achievement title, description, type, points, icon, or active status")
    public ResponseEntity<ApiResponse<AchievementResponse>> updateAchievement(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAchievementRequest request) {
        AchievementResponse response = achievementService.updateAchievement(id, request);
        return ResponseEntity.ok(ApiResponse.success("Achievement updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an achievement", description = "Soft delete achievement (sets active=false)")
    public ResponseEntity<ApiResponse<Void>> deleteAchievement(@PathVariable UUID id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.ok(ApiResponse.success("Achievement deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get paginated achievements", description = "Filter achievements by user, type, active status, and date range with pagination")
    public ResponseEntity<ApiResponse<Page<AchievementResponse>>> getAchievements(
            @RequestParam(required = false) @Parameter(description = "Filter by User UUID") UUID userId,
            @RequestParam(required = false) @Parameter(description = "Filter by achievement type") AchievementType type,
            @RequestParam(required = false) @Parameter(description = "Filter by active state") Boolean active,
            @RequestParam(required = false) @Parameter(description = "Filter earned on/after timestamp") Instant earnedFrom,
            @RequestParam(required = false) @Parameter(description = "Filter earned on/before timestamp") Instant earnedTo,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<AchievementResponse> page = achievementService.getAchievements(userId, type, active, earnedFrom, earnedTo, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user achievements", description = "Retrieve all achievements for a specific user")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getUserAchievements(@PathVariable UUID userId) {
        List<AchievementResponse> list = achievementService.getUserAchievements(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/user/{userId}/points")
    @Operation(summary = "Get user total achievement points", description = "Get aggregate sum of active achievement points for a user")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUserTotalPoints(@PathVariable UUID userId) {
        int points = achievementService.getUserTotalPoints(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("totalPoints", points)));
    }
}
