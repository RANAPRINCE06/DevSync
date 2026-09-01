package com.devsync.codingprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateCodingProfileRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    @Schema(example = "ranaprince06")
    private String username;

    @Size(max = 500, message = "Profile URL must not exceed 500 characters")
    @Schema(example = "https://leetcode.com/ranaprince06")
    private String profileUrl;

    @Min(value = 0, message = "Rating cannot be negative")
    @Schema(example = "1920")
    private Integer rating;

    @Min(value = 0, message = "Problems solved cannot be negative")
    @Schema(example = "520")
    private Integer problemsSolved;

    @Min(value = 0, message = "Contests participated cannot be negative")
    @Schema(example = "30")
    private Integer contestsParticipated;

    @Size(max = 100, message = "Rank must not exceed 100 characters")
    @Schema(example = "Knight")
    private String rank;

    @NotNull(message = "Active status is required")
    @Schema(example = "true")
    private Boolean active;

    public UpdateCodingProfileRequest() {
    }

    public UpdateCodingProfileRequest(String username, String profileUrl, Integer rating, Integer problemsSolved, Integer contestsParticipated, String rank, Boolean active) {
        this.username = username;
        this.profileUrl = profileUrl;
        this.rating = rating;
        this.problemsSolved = problemsSolved;
        this.contestsParticipated = contestsParticipated;
        this.rank = rank;
        this.active = active;
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

    public static UpdateCodingProfileRequestBuilder builder() {
        return new UpdateCodingProfileRequestBuilder();
    }

    public static class UpdateCodingProfileRequestBuilder {
        private String username;
        private String profileUrl;
        private Integer rating;
        private Integer problemsSolved;
        private Integer contestsParticipated;
        private String rank;
        private Boolean active = true;

        public UpdateCodingProfileRequestBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UpdateCodingProfileRequestBuilder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public UpdateCodingProfileRequestBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public UpdateCodingProfileRequestBuilder problemsSolved(Integer problemsSolved) {
            this.problemsSolved = problemsSolved;
            return this;
        }

        public UpdateCodingProfileRequestBuilder contestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
            return this;
        }

        public UpdateCodingProfileRequestBuilder rank(String rank) {
            this.rank = rank;
            return this;
        }

        public UpdateCodingProfileRequestBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UpdateCodingProfileRequest build() {
            return new UpdateCodingProfileRequest(username, profileUrl, rating, problemsSolved, contestsParticipated, rank, active);
        }
    }
}
