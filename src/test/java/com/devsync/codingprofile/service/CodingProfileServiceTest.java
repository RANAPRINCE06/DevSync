package com.devsync.codingprofile.service;

import com.devsync.codingprofile.dto.CreateCodingProfileRequest;
import com.devsync.codingprofile.dto.CodingProfileResponse;
import com.devsync.codingprofile.dto.UpdateCodingProfileRequest;
import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.entity.CodingProfile;
import com.devsync.codingprofile.repository.CodingProfileRepository;
import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodingProfileServiceTest {

    @Mock
    private CodingProfileRepository codingProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CodingProfileServiceImpl codingProfileService;

    private User sampleUser;
    private CodingProfile sampleProfile;
    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        sampleUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleProfile = CodingProfile.builder()
                .id(profileId)
                .user(sampleUser)
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .profileUrl("https://leetcode.com/ranaprince06")
                .rating(1850)
                .problemsSolved(450)
                .contestsParticipated(25)
                .rank("Guardian")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createCodingProfile - success")
    void createCodingProfile_Success() {
        CreateCodingProfileRequest request = CreateCodingProfileRequest.builder()
                .userId(userId)
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .profileUrl("https://leetcode.com/ranaprince06")
                .rating(1850)
                .problemsSolved(450)
                .contestsParticipated(25)
                .rank("Guardian")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(codingProfileRepository.existsByUserIdAndPlatformAndActiveTrue(userId, CodingPlatform.LEETCODE)).thenReturn(false);
        when(codingProfileRepository.save(any(CodingProfile.class))).thenReturn(sampleProfile);

        CodingProfileResponse response = codingProfileService.createCodingProfile(request);

        assertNotNull(response);
        assertEquals(profileId, response.getId());
        assertEquals(CodingPlatform.LEETCODE, response.getPlatform());
        assertEquals("ranaprince06", response.getUsername());
        verify(codingProfileRepository).save(any(CodingProfile.class));
    }

    @Test
    @DisplayName("createCodingProfile - duplicate active platform profile throws BadRequestException")
    void createCodingProfile_DuplicateActive_ThrowsException() {
        CreateCodingProfileRequest request = CreateCodingProfileRequest.builder()
                .userId(userId)
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(codingProfileRepository.existsByUserIdAndPlatformAndActiveTrue(userId, CodingPlatform.LEETCODE)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> codingProfileService.createCodingProfile(request));
        verify(codingProfileRepository, never()).save(any(CodingProfile.class));
    }

    @Test
    @DisplayName("createCodingProfile - user not found throws ResourceNotFoundException")
    void createCodingProfile_UserNotFound_ThrowsException() {
        CreateCodingProfileRequest request = CreateCodingProfileRequest.builder()
                .userId(userId)
                .platform(CodingPlatform.LEETCODE)
                .username("ranaprince06")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> codingProfileService.createCodingProfile(request));
        verify(codingProfileRepository, never()).save(any(CodingProfile.class));
    }

    @Test
    @DisplayName("getCodingProfileById - success")
    void getCodingProfileById_Success() {
        when(codingProfileRepository.findById(profileId)).thenReturn(Optional.of(sampleProfile));

        CodingProfileResponse response = codingProfileService.getCodingProfileById(profileId);

        assertNotNull(response);
        assertEquals(profileId, response.getId());
        assertEquals("Guardian", response.getRank());
    }

    @Test
    @DisplayName("updateCodingProfile - success updating fields")
    void updateCodingProfile_Success() {
        UpdateCodingProfileRequest request = UpdateCodingProfileRequest.builder()
                .username("princerana_new")
                .profileUrl("https://leetcode.com/princerana_new")
                .rating(1900)
                .problemsSolved(500)
                .contestsParticipated(28)
                .rank("Knight")
                .active(true)
                .build();

        when(codingProfileRepository.findById(profileId)).thenReturn(Optional.of(sampleProfile));
        when(codingProfileRepository.save(any(CodingProfile.class))).thenReturn(sampleProfile);

        CodingProfileResponse response = codingProfileService.updateCodingProfile(profileId, request);

        assertNotNull(response);
        assertEquals("princerana_new", sampleProfile.getUsername());
        assertEquals(1900, sampleProfile.getRating());
        verify(codingProfileRepository).save(sampleProfile);
    }

    @Test
    @DisplayName("deleteCodingProfile - soft delete sets active=false")
    void deleteCodingProfile_SoftDeletes() {
        when(codingProfileRepository.findById(profileId)).thenReturn(Optional.of(sampleProfile));
        when(codingProfileRepository.save(any(CodingProfile.class))).thenReturn(sampleProfile);

        codingProfileService.deleteCodingProfile(profileId);

        assertFalse(sampleProfile.getActive());
        verify(codingProfileRepository).save(sampleProfile);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getCodingProfiles - paginated list filtering")
    void getCodingProfiles_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CodingProfile> page = new PageImpl<>(List.of(sampleProfile));

        when(codingProfileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<CodingProfileResponse> result = codingProfileService.getCodingProfiles(userId, CodingPlatform.LEETCODE, true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ranaprince06", result.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("getUserCodingProfiles - returns user list")
    void getUserCodingProfiles_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(codingProfileRepository.findByUserId(userId)).thenReturn(List.of(sampleProfile));

        List<CodingProfileResponse> list = codingProfileService.getUserCodingProfiles(userId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(CodingPlatform.LEETCODE, list.get(0).getPlatform());
    }
}
