package com.devsync.coding.integration.adapter;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.CodingPlatformAdapter;
import com.devsync.coding.integration.CodingPlatformClient;
import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter integrating GeeksforGeeks (GFG) competitive coding statistics via the community-backed
 * public statistics service.
 *
 * In accordance with DevSync Step 6E specifications:
 * - Direct scraping of geeksforgeeks.org is strictly avoided to comply with platform terms of service.
 * - Sourced metrics are labeled as community data.
 * - Coding score, problem counts, streak data, and difficulty breakdowns (School, Basic, Easy, Medium, Hard) are tracked.
 * - Contest ratings remain null rather than fabricated.
 */
@Component
public class GeeksforGeeksAdapter implements CodingPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeeksforGeeksAdapter.class);

    private final CodingPlatformClient client;
    private final String baseUrl;
    private final boolean enabled;

    public GeeksforGeeksAdapter(CodingIntegrationProperties properties) {
        this(null, properties);
    }

    @Autowired
    public GeeksforGeeksAdapter(CodingPlatformClient client, CodingIntegrationProperties properties) {
        this.client = client;
        this.baseUrl = properties.getGeeksforgeeks() != null ? properties.getGeeksforgeeks().getBaseUrl() : "https://geeks-for-geeks-api.vercel.app";
        this.enabled = properties.getGeeksforgeeks() != null && properties.getGeeksforgeeks().isEnabled();
    }

    @Override
    public CodingPlatform getPlatform() {
        return CodingPlatform.GEEKSFORGEEKS;
    }

    @Override
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_.-]{3,50}$");
    }

    @Override
    public ExternalProfileData fetchProfileData(String username) {
        if (!validateUsername(username)) {
            throw new PlatformIntegrationException("Invalid GeeksforGeeks username format: " + username);
        }

        if (!enabled || client == null) {
            throw new PlatformUnsupportedException("GeeksforGeeks integration is currently disabled in configuration.");
        }

        String targetUrl = baseUrl + "/" + username;
        GfgApiResponse response;
        try {
            response = client.get(targetUrl, GfgApiResponse.class, null);
        } catch (PlatformUserNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PlatformIntegrationException("Failed to fetch GeeksforGeeks statistics for user: " + username + " - " + ex.getMessage(), ex);
        }

        if (response == null || (response.getInfo() == null && response.getTotalProblemsSolved() == null && response.getSchool() == null)) {
            throw new PlatformUserNotFoundException("GeeksforGeeks user not found or empty profile: " + username);
        }

        GfgInfo info = response.getInfo();
        GfgSolvedStats solvedStats = response.getSolvedStats();

        int schoolCount = 0;
        int basicCount = 0;
        int easyCount = 0;
        int mediumCount = 0;
        int hardCount = 0;

        if (solvedStats != null) {
            if (solvedStats.getSchool() != null && solvedStats.getSchool().getCount() != null) {
                schoolCount = solvedStats.getSchool().getCount();
            }
            if (solvedStats.getBasic() != null && solvedStats.getBasic().getCount() != null) {
                basicCount = solvedStats.getBasic().getCount();
            }
            if (solvedStats.getEasy() != null && solvedStats.getEasy().getCount() != null) {
                easyCount = solvedStats.getEasy().getCount();
            }
            if (solvedStats.getMedium() != null && solvedStats.getMedium().getCount() != null) {
                mediumCount = solvedStats.getMedium().getCount();
            }
            if (solvedStats.getHard() != null && solvedStats.getHard().getCount() != null) {
                hardCount = solvedStats.getHard().getCount();
            }
        } else {
            // Check fallback flat structure
            if (response.getSchool() != null) schoolCount = response.getSchool();
            if (response.getBasic() != null) basicCount = response.getBasic();
            if (response.getEasy() != null) easyCount = response.getEasy();
            if (response.getMedium() != null) mediumCount = response.getMedium();
            if (response.getHard() != null) hardCount = response.getHard();
        }

        int calculatedTotal = schoolCount + basicCount + easyCount + mediumCount + hardCount;
        Integer totalProblemsSolved = calculatedTotal;

        if (info != null && info.getTotalProblemsSolved() != null) {
            try {
                totalProblemsSolved = Integer.parseInt(info.getTotalProblemsSolved().trim());
            } catch (NumberFormatException ignored) {
                // fall back to calculatedTotal
            }
        } else if (response.getTotalProblemsSolved() != null) {
            totalProblemsSolved = response.getTotalProblemsSolved();
        }

        // Map difficulty: school + basic + easy -> easySolved; medium -> mediumSolved; hard -> hardSolved
        int easyTierSolved = schoolCount + basicCount + easyCount;

        Map<String, Object> additionalStats = new HashMap<>();
        additionalStats.put("dataSource", "GeeksforGeeks Community API");
        additionalStats.put("schoolSolved", schoolCount);
        additionalStats.put("basicSolved", basicCount);
        additionalStats.put("easySolved", easyCount);
        additionalStats.put("mediumSolved", mediumCount);
        additionalStats.put("hardSolved", hardCount);

        if (info != null) {
            if (info.getUserName() != null && !info.getUserName().isBlank()) {
                additionalStats.put("displayName", info.getUserName());
            }
            if (info.getProfilePicture() != null && !info.getProfilePicture().isBlank()) {
                additionalStats.put("avatarUrl", info.getProfilePicture());
            }
            if (info.getCodingScore() != null) {
                try {
                    additionalStats.put("codingScore", Integer.parseInt(info.getCodingScore().trim()));
                } catch (NumberFormatException ignored) {
                    additionalStats.put("codingScore", info.getCodingScore());
                }
            }
            if (info.getInstituteRank() != null && !info.getInstituteRank().isBlank()) {
                additionalStats.put("instituteRank", info.getInstituteRank().trim());
            }
            if (info.getCurrentStreak() != null) {
                try {
                    additionalStats.put("currentStreak", Integer.parseInt(info.getCurrentStreak().trim()));
                } catch (NumberFormatException ignored) {
                    additionalStats.put("currentStreak", info.getCurrentStreak());
                }
            }
            if (info.getMaxStreak() != null) {
                try {
                    additionalStats.put("maxStreak", Integer.parseInt(info.getMaxStreak().trim()));
                } catch (NumberFormatException ignored) {
                    additionalStats.put("maxStreak", info.getMaxStreak());
                }
            }
            if (info.getInstitution() != null && !info.getInstitution().isBlank()) {
                additionalStats.put("institution", info.getInstitution().trim());
            }
        }

        return new ExternalProfileData(
                CodingPlatform.GEEKSFORGEEKS,
                username,
                CodingPlatform.GEEKSFORGEEKS.buildProfileUrl(username),
                null, // currentRating remains null (GFG does not have contest rating)
                null, // maximumRating
                null, // globalRank
                null, // countryRank
                totalProblemsSolved,
                easyTierSolved,
                mediumCount,
                hardCount,
                null, // contestsParticipated
                null, // platformRank
                additionalStats,
                Instant.now()
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GfgApiResponse {
        @JsonProperty("info")
        private GfgInfo info;

        @JsonProperty("solvedStats")
        private GfgSolvedStats solvedStats;

        // Fallback flat fields supported by alternative community endpoints
        @JsonProperty("userName")
        private String userName;

        @JsonProperty("totalProblemsSolved")
        private Integer totalProblemsSolved;

        @JsonProperty("School")
        private Integer school;

        @JsonProperty("Basic")
        private Integer basic;

        @JsonProperty("Easy")
        private Integer easy;

        @JsonProperty("Medium")
        private Integer medium;

        @JsonProperty("Hard")
        private Integer hard;

        public GfgInfo getInfo() {
            return info;
        }

        public void setInfo(GfgInfo info) {
            this.info = info;
        }

        public GfgSolvedStats getSolvedStats() {
            return solvedStats;
        }

        public void setSolvedStats(GfgSolvedStats solvedStats) {
            this.solvedStats = solvedStats;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Integer getTotalProblemsSolved() {
            return totalProblemsSolved;
        }

        public void setTotalProblemsSolved(Integer totalProblemsSolved) {
            this.totalProblemsSolved = totalProblemsSolved;
        }

        public Integer getSchool() {
            return school;
        }

        public void setSchool(Integer school) {
            this.school = school;
        }

        public Integer getBasic() {
            return basic;
        }

        public void setBasic(Integer basic) {
            this.basic = basic;
        }

        public Integer getEasy() {
            return easy;
        }

        public void setEasy(Integer easy) {
            this.easy = easy;
        }

        public Integer getMedium() {
            return medium;
        }

        public void setMedium(Integer medium) {
            this.medium = medium;
        }

        public Integer getHard() {
            return hard;
        }

        public void setHard(Integer hard) {
            this.hard = hard;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GfgInfo {
        @JsonProperty("userName")
        private String userName;

        @JsonProperty("profilePicture")
        private String profilePicture;

        @JsonProperty("instituteRank")
        private String instituteRank;

        @JsonProperty("currentStreak")
        private String currentStreak;

        @JsonProperty("maxStreak")
        private String maxStreak;

        @JsonProperty("institution")
        private String institution;

        @JsonProperty("codingScore")
        private String codingScore;

        @JsonProperty("totalProblemsSolved")
        private String totalProblemsSolved;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getInstituteRank() {
            return instituteRank;
        }

        public void setInstituteRank(String instituteRank) {
            this.instituteRank = instituteRank;
        }

        public String getCurrentStreak() {
            return currentStreak;
        }

        public void setCurrentStreak(String currentStreak) {
            this.currentStreak = currentStreak;
        }

        public String getMaxStreak() {
            return maxStreak;
        }

        public void setMaxStreak(String maxStreak) {
            this.maxStreak = maxStreak;
        }

        public String getInstitution() {
            return institution;
        }

        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public String getCodingScore() {
            return codingScore;
        }

        public void setCodingScore(String codingScore) {
            this.codingScore = codingScore;
        }

        public String getTotalProblemsSolved() {
            return totalProblemsSolved;
        }

        public void setTotalProblemsSolved(String totalProblemsSolved) {
            this.totalProblemsSolved = totalProblemsSolved;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GfgSolvedStats {
        @JsonProperty("school")
        private GfgCountEntry school;

        @JsonProperty("basic")
        private GfgCountEntry basic;

        @JsonProperty("easy")
        private GfgCountEntry easy;

        @JsonProperty("medium")
        private GfgCountEntry medium;

        @JsonProperty("hard")
        private GfgCountEntry hard;

        public GfgCountEntry getSchool() {
            return school;
        }

        public void setSchool(GfgCountEntry school) {
            this.school = school;
        }

        public GfgCountEntry getBasic() {
            return basic;
        }

        public void setBasic(GfgCountEntry basic) {
            this.basic = basic;
        }

        public GfgCountEntry getEasy() {
            return easy;
        }

        public void setEasy(GfgCountEntry easy) {
            this.easy = easy;
        }

        public GfgCountEntry getMedium() {
            return medium;
        }

        public void setMedium(GfgCountEntry medium) {
            this.medium = medium;
        }

        public GfgCountEntry getHard() {
            return hard;
        }

        public void setHard(GfgCountEntry hard) {
            this.hard = hard;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GfgCountEntry {
        @JsonProperty("count")
        private Integer count;

        public GfgCountEntry() {
        }

        public GfgCountEntry(Integer count) {
            this.count = count;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
