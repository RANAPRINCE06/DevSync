package com.devsync.codingprofile.entity;

import com.devsync.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coding_profiles")
public class CodingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CodingPlatform platform;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "profile_url", length = 500)
    private String profileUrl;

    @Column
    private Integer rating;

    @Column(name = "problems_solved")
    private Integer problemsSolved;

    @Column(name = "contests_participated")
    private Integer contestsParticipated;

    @Column(length = 100)
    private String rank;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CodingProfile() {
    }

    public CodingProfile(UUID id, User user, CodingPlatform platform, String username, String profileUrl, Integer rating, Integer problemsSolved, Integer contestsParticipated, String rank, Boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.rating = rating;
        this.problemsSolved = problemsSolved;
        this.contestsParticipated = contestsParticipated;
        this.rank = rank;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public static CodingProfileBuilder builder() {
        return new CodingProfileBuilder();
    }

    public static class CodingProfileBuilder {
        private UUID id;
        private User user;
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

        public CodingProfileBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CodingProfileBuilder user(User user) {
            this.user = user;
            return this;
        }

        public CodingProfileBuilder platform(CodingPlatform platform) {
            this.platform = platform;
            return this;
        }

        public CodingProfileBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CodingProfileBuilder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public CodingProfileBuilder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public CodingProfileBuilder problemsSolved(Integer problemsSolved) {
            this.problemsSolved = problemsSolved;
            return this;
        }

        public CodingProfileBuilder contestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
            return this;
        }

        public CodingProfileBuilder rank(String rank) {
            this.rank = rank;
            return this;
        }

        public CodingProfileBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public CodingProfileBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CodingProfileBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CodingProfile build() {
            return new CodingProfile(id, user, platform, username, profileUrl, rating, problemsSolved, contestsParticipated, rank, active, createdAt, updatedAt);
        }
    }
}
