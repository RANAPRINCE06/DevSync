package com.devsync.codingprofile.service;

import com.devsync.codingprofile.dto.CreateCodingProfileRequest;
import com.devsync.codingprofile.dto.CodingProfileResponse;
import com.devsync.codingprofile.dto.UpdateCodingProfileRequest;
import com.devsync.codingprofile.entity.CodingPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CodingProfileService {

    CodingProfileResponse createCodingProfile(CreateCodingProfileRequest request);

    CodingProfileResponse getCodingProfileById(UUID id);

    CodingProfileResponse updateCodingProfile(UUID id, UpdateCodingProfileRequest request);

    void deleteCodingProfile(UUID id);

    Page<CodingProfileResponse> getCodingProfiles(UUID userId, CodingPlatform platform, Boolean active, Pageable pageable);

    List<CodingProfileResponse> getUserCodingProfiles(UUID userId);
}
