package com.devsync.coding.analytics.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.List;

public class ProblemDistributionResponse {

    private String period;
    private Instant from;
    private Instant to;
    private Integer totalPlatformReportedSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer unclassifiedSolved;
    private Integer solvedDeltaInPeriod;
    private boolean verifiedUniqueDeduplication;
    private String deduplicationNote;
    private List<PlatformProblemBreakdown> platformBreakdowns;

    public ProblemDistributionResponse() {
    }

    public ProblemDistributionResponse(String period, Instant from, Instant to,
                                       Integer totalPlatformReportedSolved, Integer easySolved,
                                       Integer mediumSolved, Integer hardSolved, Integer unclassifiedSolved,
                                       Integer solvedDeltaInPeriod, boolean verifiedUniqueDeduplication,
                                       String deduplicationNote,
                                       List<PlatformProblemBreakdown> platformBreakdowns) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.totalPlatformReportedSolved = totalPlatformReportedSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.unclassifiedSolved = unclassifiedSolved;
        this.solvedDeltaInPeriod = solvedDeltaInPeriod;
        this.verifiedUniqueDeduplication = verifiedUniqueDeduplication;
        this.deduplicationNote = deduplicationNote;
        this.platformBreakdowns = platformBreakdowns;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public Integer getTotalPlatformReportedSolved() {
        return totalPlatformReportedSolved;
    }

    public void setTotalPlatformReportedSolved(Integer totalPlatformReportedSolved) {
        this.totalPlatformReportedSolved = totalPlatformReportedSolved;
    }

    public Integer getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(Integer easySolved) {
        this.easySolved = easySolved;
    }

    public Integer getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(Integer mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public Integer getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(Integer hardSolved) {
        this.hardSolved = hardSolved;
    }

    public Integer getUnclassifiedSolved() {
        return unclassifiedSolved;
    }

    public void setUnclassifiedSolved(Integer unclassifiedSolved) {
        this.unclassifiedSolved = unclassifiedSolved;
    }

    public Integer getSolvedDeltaInPeriod() {
        return solvedDeltaInPeriod;
    }

    public void setSolvedDeltaInPeriod(Integer solvedDeltaInPeriod) {
        this.solvedDeltaInPeriod = solvedDeltaInPeriod;
    }

    public boolean isVerifiedUniqueDeduplication() {
        return verifiedUniqueDeduplication;
    }

    public void setVerifiedUniqueDeduplication(boolean verifiedUniqueDeduplication) {
        this.verifiedUniqueDeduplication = verifiedUniqueDeduplication;
    }

    public String getDeduplicationNote() {
        return deduplicationNote;
    }

    public void setDeduplicationNote(String deduplicationNote) {
        this.deduplicationNote = deduplicationNote;
    }

    public List<PlatformProblemBreakdown> getPlatformBreakdowns() {
        return platformBreakdowns;
    }

    public void setPlatformBreakdowns(List<PlatformProblemBreakdown> platformBreakdowns) {
        this.platformBreakdowns = platformBreakdowns;
    }

    public static class PlatformProblemBreakdown {
        private CodingPlatform platform;
        private String username;
        private Integer totalSolved;
        private Integer easySolved;
        private Integer mediumSolved;
        private Integer hardSolved;
        private Double percentageOfTotal;
        private Integer deltaInPeriod;

        public PlatformProblemBreakdown() {
        }

        public PlatformProblemBreakdown(CodingPlatform platform, String username, Integer totalSolved,
                                        Integer easySolved, Integer mediumSolved, Integer hardSolved,
                                        Double percentageOfTotal, Integer deltaInPeriod) {
            this.platform = platform;
            this.username = username;
            this.totalSolved = totalSolved;
            this.easySolved = easySolved;
            this.mediumSolved = mediumSolved;
            this.hardSolved = hardSolved;
            this.percentageOfTotal = percentageOfTotal;
            this.deltaInPeriod = deltaInPeriod;
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

        public Integer getTotalSolved() {
            return totalSolved;
        }

        public void setTotalSolved(Integer totalSolved) {
            this.totalSolved = totalSolved;
        }

        public Integer getEasySolved() {
            return easySolved;
        }

        public void setEasySolved(Integer easySolved) {
            this.easySolved = easySolved;
        }

        public Integer getMediumSolved() {
            return mediumSolved;
        }

        public void setMediumSolved(Integer mediumSolved) {
            this.mediumSolved = mediumSolved;
        }

        public Integer getHardSolved() {
            return hardSolved;
        }

        public void setHardSolved(Integer hardSolved) {
            this.hardSolved = hardSolved;
        }

        public Double getPercentageOfTotal() {
            return percentageOfTotal;
        }

        public void setPercentageOfTotal(Double percentageOfTotal) {
            this.percentageOfTotal = percentageOfTotal;
        }

        public Integer getDeltaInPeriod() {
            return deltaInPeriod;
        }

        public void setDeltaInPeriod(Integer deltaInPeriod) {
            this.deltaInPeriod = deltaInPeriod;
        }
    }
}
