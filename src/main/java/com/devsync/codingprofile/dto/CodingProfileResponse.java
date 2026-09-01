package com.devsync.codingprofile.dto;

import com.devsync.codingprofile.entity.CodingPlatform;
import com.devsync.codingprofile.entity.CodingProfile;

import java.time.Instant;
import java.util.UUID;

public class CodingProfileResponse {

    private UUID id;
    private UUID userId;
    private String userName;
    private String userEmail;
    private CodingPlatform platform;
    private String username;
    private String profileUrl;
    private Integer rating;
    private Integer problemsSolved;
    private Integer contestsParticipated;
    private String rank;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public CodingProfileResponse() {
    }

    public CodingProfileResponse(UUID id, UUID userId, String userName, String userEmail, CodingPlatform platform, String username, String profileUrl, Integer rating, Integer problemsSolved, Integer contestsParticipated, String rank, Boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.rating = rating;
        this.problemsSolved = problemsSolved;
        this.contestsParticipated = contestsParticipated;
        this.rank = rank;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CodingProfileResponse fromEntity(CodingProfile profile) {
        return CodingProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .userName(profile.getUser().getName())
                .userEmail(profile.getUser().getEmail())
                .platform(profile.getPlatform())
                .username(profile.getUsername())
                .profileUrl(profile.getProfileUrl())
                .rating(profile.getRating())
                .problemsSolved(profile.getProblemsSolved())
                .contestsParticipated(profile.getContestsParticipated())
                .rank(profile.getRank())
                .active(profile.getActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public CodingPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(CodingPlatform platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(Integer problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public Integer getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(Integer contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static CodingProfileResponseBuilder builder() {
        return new CodingProfileResponseBuilder();
    }

    public static class CodingProfileResponseBuilder {
        private UUID id;
        private UUID userId;
        private String userName;
        private String userEmail;
        private CodingPlatform platform;
        private String username;
        private String profileUrl;
        private Integer rating;
        private Integer problemsSolved;
        private Integer contestsParticipated;
        private String rank;
        private Boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public CodingProfileResponseBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CodingProfileResponseBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public CodingProfileResponseBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public CodingProfileResponseBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public CodingProfileResponseBuilder platform(CodingPlatform platform) {
            this.platform = platform;
            return this;
        }

        public CodingProfileResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CodingProfileResponseBuilder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public CodingProfileResponseBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public CodingProfileResponseBuilder problemsSolved(Integer problemsSolved) {
            this.problemsSolved = problemsSolved;
            return this;
        }

        public CodingProfileResponseBuilder contestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
            return this;
        }

        public CodingProfileResponseBuilder rank(String rank) {
            this.rank = rank;
            return this;
        }

        public CodingProfileResponseBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public CodingProfileResponseBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CodingProfileResponseBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CodingProfileResponse build() {
            return new CodingProfileResponse(id, userId, userName, userEmail, platform, username, profileUrl, rating, problemsSolved, contestsParticipated, rank, active, createdAt, updatedAt);
        }
    }
}
