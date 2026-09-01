package com.devsync.codingprofile.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.codingprofile.dto.CreateCodingProfileRequest;
import com.devsync.codingprofile.dto.CodingProfileResponse;
import com.devsync.codingprofile.dto.UpdateCodingProfileRequest;
import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.entity.CodingProfile;
import com.devsync.codingprofile.repository.CodingProfileRepository;
import com.devsync.codingprofile.repository.CodingProfileSpecification;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CodingProfileServiceImpl implements CodingProfileService {

    private final CodingProfileRepository codingProfileRepository;
    private final UserRepository userRepository;

    public CodingProfileServiceImpl(CodingProfileRepository codingProfileRepository, UserRepository userRepository) {
        this.codingProfileRepository = codingProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CodingProfileResponse createCodingProfile(CreateCodingProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (codingProfileRepository.existsByUserIdAndPlatformAndActiveTrue(user.getId(), request.getPlatform())) {
            throw new BadRequestException("Active profile already exists for platform: " + request.getPlatform());
        }

        CodingProfile profile = CodingProfile.builder()
                .user(user)
                .platform(request.getPlatform())
                .username(request.getUsername().trim())
                .profileUrl(request.getProfileUrl() != null ? request.getProfileUrl().trim() : null)
                .rating(request.getRating())
                .problemsSolved(request.getProblemsSolved())
                .contestsParticipated(request.getContestsParticipated())
                .rank(request.getRank() != null ? request.getRank().trim() : null)
                .active(true)
                .build();

        CodingProfile saved = codingProfileRepository.save(profile);
        return CodingProfileResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CodingProfileResponse getCodingProfileById(UUID id) {
        CodingProfile profile = codingProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coding profile not found with id: " + id));
        return CodingProfileResponse.fromEntity(profile);
    }

    @Override
    @Transactional
    public CodingProfileResponse updateCodingProfile(UUID id, UpdateCodingProfileRequest request) {
        CodingProfile profile = codingProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coding profile not found with id: " + id));

        if (Boolean.TRUE.equals(request.getActive()) && !profile.getActive()) {
            Optional<CodingProfile> existingActive = codingProfileRepository.findByUserIdAndPlatformAndActiveTrue(
                    profile.getUser().getId(), profile.getPlatform()
            );
            if (existingActive.isPresent() && !existingActive.get().getId().equals(profile.getId())) {
                throw new BadRequestException("Another active profile already exists for platform: " + profile.getPlatform());
            }
        }

        profile.setUsername(request.getUsername().trim());
        profile.setProfileUrl(request.getProfileUrl() != null ? request.getProfileUrl().trim() : null);
        profile.setRating(request.getRating());
        profile.setProblemsSolved(request.getProblemsSolved());
        profile.setContestsParticipated(request.getContestsParticipated());
        profile.setRank(request.getRank() != null ? request.getRank().trim() : null);
        profile.setActive(request.getActive());

        CodingProfile saved = codingProfileRepository.save(profile);
        return CodingProfileResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteCodingProfile(UUID id) {
        CodingProfile profile = codingProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coding profile not found with id: " + id));
        profile.setActive(false);
        codingProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CodingProfileResponse> getCodingProfiles(UUID userId, CodingPlatform platform, Boolean active, Pageable pageable) {
        Specification<CodingProfile> spec = CodingProfileSpecification.filter(userId, platform, active);
        return codingProfileRepository.findAll(spec, pageable).map(CodingProfileResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingProfileResponse> getUserCodingProfiles(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return codingProfileRepository.findByUserId(userId).stream()
                .map(CodingProfileResponse::fromEntity)
                .toList();
    }
}
