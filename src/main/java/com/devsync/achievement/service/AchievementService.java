package com.devsync.achievement.service;

import com.devsync.achievement.dto.CreateAchievementRequest;
import com.devsync.achievement.dto.AchievementResponse;
import com.devsync.achievement.dto.UpdateAchievementRequest;
import com.devsync.achievement.entity.AchievementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AchievementService {

    AchievementResponse createAchievement(CreateAchievementRequest request);

    AchievementResponse getAchievementById(UUID id);

    AchievementResponse updateAchievement(UUID id, UpdateAchievementRequest request);

    void deleteAchievement(UUID id);

    Page<AchievementResponse> getAchievements(UUID userId, AchievementType type, Boolean active, Instant earnedFrom, Instant earnedTo, Pageable pageable);

    List<AchievementResponse> getUserAchievements(UUID userId);

    int getUserTotalPoints(UUID userId);
}
