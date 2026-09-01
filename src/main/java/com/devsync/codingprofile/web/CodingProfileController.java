package com.devsync.codingprofile.web;

import com.devsync.codingprofile.dto.CreateCodingProfileRequest;
import com.devsync.codingprofile.dto.CodingProfileResponse;
import com.devsync.codingprofile.dto.UpdateCodingProfileRequest;
import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.service.CodingProfileService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coding-profiles")
@Tag(name = "Coding Profile Management", description = "Endpoints for linking and managing user competitive programming & developer profiles")
public class CodingProfileController {

    private final CodingProfileService codingProfileService;

    public CodingProfileController(CodingProfileService codingProfileService) {
        this.codingProfileService = codingProfileService;
    }

    @PostMapping
    @Operation(summary = "Create coding profile", description = "Link a developer/coding platform profile for a user")
    public ResponseEntity<ApiResponse<CodingProfileResponse>> createCodingProfile(@Valid @RequestBody CreateCodingProfileRequest request) {
        CodingProfileResponse response = codingProfileService.createCodingProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coding profile created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coding profile by ID", description = "Retrieve coding profile details by UUID")
    public ResponseEntity<ApiResponse<CodingProfileResponse>> getCodingProfileById(@PathVariable UUID id) {
        CodingProfileResponse response = codingProfileService.getCodingProfileById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update coding profile", description = "Update username, stats, rating, rank, or active state")
    public ResponseEntity<ApiResponse<CodingProfileResponse>> updateCodingProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCodingProfileRequest request) {
        CodingProfileResponse response = codingProfileService.updateCodingProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coding profile updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coding profile", description = "Soft delete coding profile (sets active=false)")
    public ResponseEntity<ApiResponse<Void>> deleteCodingProfile(@PathVariable UUID id) {
        codingProfileService.deleteCodingProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Coding profile deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get paginated coding profiles", description = "Filter coding profiles by user, platform, and active state with pagination")
    public ResponseEntity<ApiResponse<Page<CodingProfileResponse>>> getCodingProfiles(
            @RequestParam(required = false) @Parameter(description = "Filter by User UUID") UUID userId,
            @RequestParam(required = false) @Parameter(description = "Filter by platform") CodingPlatform platform,
            @RequestParam(required = false) @Parameter(description = "Filter by active state") Boolean active,
            @ParameterObject
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<CodingProfileResponse> page = codingProfileService.getCodingProfiles(userId, platform, active, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user coding profiles", description = "Retrieve all coding profiles linked to a user")
    public ResponseEntity<ApiResponse<List<CodingProfileResponse>>> getUserCodingProfiles(@PathVariable UUID userId) {
        List<CodingProfileResponse> list = codingProfileService.getUserCodingProfiles(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
