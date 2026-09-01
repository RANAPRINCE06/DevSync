package com.devsync.achievement.service;

import com.devsync.achievement.dto.CreateAchievementRequest;
import com.devsync.achievement.dto.AchievementResponse;
import com.devsync.achievement.dto.UpdateAchievementRequest;
import com.devsync.achievement.entity.Achievement;
import com.devsync.achievement.entity.AchievementType;
import com.devsync.achievement.repository.AchievementRepository;
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
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    private User sampleUser;
    private Achievement sampleAchievement;
    private UUID userId;
    private UUID achievementId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        achievementId = UUID.randomUUID();

        sampleUser = User.builder()
                .id(userId)
                .name("Prince")
                .email("prince@devsync.com")
                .timezone("Asia/Kolkata")
                .active(true)
                .build();

        sampleAchievement = Achievement.builder()
                .id(achievementId)
                .user(sampleUser)
                .title("7-Day Streak")
                .description("Completed 7 days streak")
                .type(AchievementType.STREAK)
                .points(100)
                .earnedAt(Instant.now())
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("createAchievement - success")
    void createAchievement_Success() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .userId(userId)
                .title("7-Day Streak")
                .description("Completed 7 days streak")
                .type(AchievementType.STREAK)
                .points(100)
                .earnedAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(achievementRepository.save(any(Achievement.class))).thenReturn(sampleAchievement);

        AchievementResponse response = achievementService.createAchievement(request);

        assertNotNull(response);
        assertEquals(achievementId, response.getId());
        assertEquals("7-Day Streak", response.getTitle());
        assertEquals(100, response.getPoints());
        verify(achievementRepository).save(any(Achievement.class));
    }

    @Test
    @DisplayName("createAchievement - user not found throws ResourceNotFoundException")
    void createAchievement_UserNotFound_ThrowsException() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .userId(userId)
                .title("Title")
                .type(AchievementType.STREAK)
                .points(100)
                .earnedAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> achievementService.createAchievement(request));
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    @DisplayName("getAchievementById - success")
    void getAchievementById_Success() {
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(sampleAchievement));

        AchievementResponse response = achievementService.getAchievementById(achievementId);

        assertNotNull(response);
        assertEquals(achievementId, response.getId());
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("updateAchievement - success")
    void updateAchievement_Success() {
        UpdateAchievementRequest request = UpdateAchievementRequest.builder()
                .title("Updated Streak")
                .description("Updated description")
                .type(AchievementType.STREAK)
                .points(150)
                .earnedAt(Instant.now())
                .active(true)
                .build();

        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(sampleAchievement));
        when(achievementRepository.save(any(Achievement.class))).thenReturn(sampleAchievement);

        AchievementResponse response = achievementService.updateAchievement(achievementId, request);

        assertNotNull(response);
        assertEquals("Updated Streak", sampleAchievement.getTitle());
        assertEquals(150, sampleAchievement.getPoints());
        verify(achievementRepository).save(sampleAchievement);
    }

    @Test
    @DisplayName("deleteAchievement - soft delete sets active=false")
    void deleteAchievement_SoftDeletes() {
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(sampleAchievement));
        when(achievementRepository.save(any(Achievement.class))).thenReturn(sampleAchievement);

        achievementService.deleteAchievement(achievementId);

        assertFalse(sampleAchievement.getActive());
        verify(achievementRepository).save(sampleAchievement);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getAchievements - paginated filtering")
    void getAchievements_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Achievement> page = new PageImpl<>(List.of(sampleAchievement));

        when(achievementRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<AchievementResponse> result = achievementService.getAchievements(userId, AchievementType.STREAK, true, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("7-Day Streak", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("getUserAchievements - returns list")
    void getUserAchievements_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(achievementRepository.findByUserId(userId)).thenReturn(List.of(sampleAchievement));

        List<AchievementResponse> list = achievementService.getUserAchievements(userId);

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("getUserTotalPoints - aggregates active points")
    void getUserTotalPoints_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(achievementRepository.sumPointsByUserIdAndActiveTrue(userId)).thenReturn(350);

        int points = achievementService.getUserTotalPoints(userId);

        assertEquals(350, points);
    }
}
