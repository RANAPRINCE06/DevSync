package com.devsync.coding.analytics.dto;

import com.devsync.coding.enums.CodingPlatform;

import java.time.Instant;
import java.util.List;

public class PerformanceInsightsResponse {

    private String period;
    private Instant from;
    private Instant to;
    private int insightsCount;
    private List<PerformanceInsightItem> insights;

    public PerformanceInsightsResponse() {
    }

    public PerformanceInsightsResponse(String period, Instant from, Instant to,
                                       int insightsCount, List<PerformanceInsightItem> insights) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.insightsCount = insightsCount;
        this.insights = insights;
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

    public int getInsightsCount() {
        return insightsCount;
    }

    public void setInsightsCount(int insightsCount) {
        this.insightsCount = insightsCount;
    }

    public List<PerformanceInsightItem> getInsights() {
        return insights;
    }

    public void setInsights(List<PerformanceInsightItem> insights) {
        this.insights = insights;
    }

    public static class PerformanceInsightItem {
        private String id;
        private CodingPlatform platform;
        private String category; // RATING, SOLVED, CONTEST, DATA_COVERAGE
        private String type;     // POSITIVE, NEUTRAL, INFORMATIONAL, ATTENTION
        private String title;
        private String message;
        private String metricValue;

        public PerformanceInsightItem() {
        }

        public PerformanceInsightItem(String id, CodingPlatform platform, String category,
                                      String type, String title, String message, String metricValue) {
            this.id = id;
            this.platform = platform;
            this.category = category;
            this.type = type;
            this.title = title;
            this.message = message;
            this.metricValue = metricValue;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public CodingPlatform getPlatform() {
            return platform;
        }

        public void setPlatform(CodingPlatform platform) {
            this.platform = platform;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMetricValue() {
            return metricValue;
        }

        public void setMetricValue(String metricValue) {
            this.metricValue = metricValue;
        }
    }
}
