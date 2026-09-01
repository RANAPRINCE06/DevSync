package com.devsync.achievement.service;

import com.devsync.achievement.dto.CreateAchievementRequest;
import com.devsync.achievement.dto.AchievementResponse;
import com.devsync.achievement.dto.UpdateAchievementRequest;
import com.devsync.achievement.entity.Achievement;
import com.devsync.achievement.entity.AchievementType;
import com.devsync.achievement.repository.AchievementRepository;
import com.devsync.achievement.repository.AchievementSpecification;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.user.entity.User;
import com.devsync.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public AchievementServiceImpl(AchievementRepository achievementRepository, UserRepository userRepository) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Achievement achievement = Achievement.builder()
                .user(user)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .type(request.getType())
                .icon(request.getIcon() != null ? request.getIcon().trim() : null)
                .points(request.getPoints())
                .earnedAt(request.getEarnedAt())
                .active(true)
                .build();

        Achievement saved = achievementRepository.save(achievement);
        return AchievementResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AchievementResponse getAchievementById(UUID id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));
        return AchievementResponse.fromEntity(achievement);
    }

    @Override
    @Transactional
    public AchievementResponse updateAchievement(UUID id, UpdateAchievementRequest request) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));

        achievement.setTitle(request.getTitle().trim());
        achievement.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        achievement.setType(request.getType());
        achievement.setIcon(request.getIcon() != null ? request.getIcon().trim() : null);
        achievement.setPoints(request.getPoints());
        achievement.setEarnedAt(request.getEarnedAt());
        achievement.setActive(request.getActive());

        Achievement saved = achievementRepository.save(achievement);
        return AchievementResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteAchievement(UUID id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));
        achievement.setActive(false);
        achievementRepository.save(achievement);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AchievementResponse> getAchievements(UUID userId, AchievementType type, Boolean active, Instant earnedFrom, Instant earnedTo, Pageable pageable) {
        Specification<Achievement> spec = AchievementSpecification.filter(userId, type, active, earnedFrom, earnedTo);
        return achievementRepository.findAll(spec, pageable).map(AchievementResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> getUserAchievements(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return achievementRepository.findByUserId(userId).stream()
                .map(AchievementResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserTotalPoints(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        Integer points = achievementRepository.sumPointsByUserIdAndActiveTrue(userId);
        return points != null ? points : 0;
    }
}
