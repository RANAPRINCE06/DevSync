package com.devsync.codingprofile.dto;

import com.devsync.codingprofile.entity.CodingPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateCodingProfileRequest {

    @NotNull(message = "User ID is required")
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotNull(message = "Coding platform is required")
    @Schema(example = "LEETCODE")
    private CodingPlatform platform;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    @Schema(example = "ranaprince06")
    private String username;

    @Size(max = 500, message = "Profile URL must not exceed 500 characters")
    @Schema(example = "https://leetcode.com/ranaprince06")
    private String profileUrl;

    @Min(value = 0, message = "Rating cannot be negative")
    @Schema(example = "1850")
    private Integer rating;

    @Min(value = 0, message = "Problems solved cannot be negative")
    @Schema(example = "450")
    private Integer problemsSolved;

    @Min(value = 0, message = "Contests participated cannot be negative")
    @Schema(example = "25")
    private Integer contestsParticipated;

    @Size(max = 100, message = "Rank must not exceed 100 characters")
    @Schema(example = "Guardian")
    private String rank;

    public CreateCodingProfileRequest() {
    }

    public CreateCodingProfileRequest(UUID userId, CodingPlatform platform, String username, String profileUrl, Integer rating, Integer problemsSolved, Integer contestsParticipated, String rank) {
        this.userId = userId;
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.rating = rating;
        this.problemsSolved = problemsSolved;
        this.contestsParticipated = contestsParticipated;
        this.rank = rank;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public static CreateCodingProfileRequestBuilder builder() {
        return new CreateCodingProfileRequestBuilder();
    }

    public static class CreateCodingProfileRequestBuilder {
        private UUID userId;
        private CodingPlatform platform;
        private String username;
        private String profileUrl;
        private Integer rating;
        private Integer problemsSolved;
        private Integer contestsParticipated;
        private String rank;

        public CreateCodingProfileRequestBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public CreateCodingProfileRequestBuilder platform(CodingPlatform platform) {
            this.platform = platform;
            return this;
        }

        public CreateCodingProfileRequestBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CreateCodingProfileRequestBuilder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public CreateCodingProfileRequestBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public CreateCodingProfileRequestBuilder problemsSolved(Integer problemsSolved) {
            this.problemsSolved = problemsSolved;
            return this;
        }

        public CreateCodingProfileRequestBuilder contestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
            return this;
        }

        public CreateCodingProfileRequestBuilder rank(String rank) {
            this.rank = rank;
            return this;
        }

        public CreateCodingProfileRequest build() {
            return new CreateCodingProfileRequest(userId, platform, username, profileUrl, rating, problemsSolved, contestsParticipated, rank);
        }
    }
}
