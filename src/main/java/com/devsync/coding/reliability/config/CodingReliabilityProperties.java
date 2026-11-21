package com.devsync.coding.reliability.config;

import com.devsync.coding.enums.CodingPlatform;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized reliability configuration covering rate limiting, profile caching, and retry policies.
 */
@Component
@ConfigurationProperties(prefix = "app.coding.reliability")
public class CodingReliabilityProperties {

    private RateLimitProperties rateLimit = new RateLimitProperties();
    private CacheProperties cache = new CacheProperties();
    private RetryProperties retry = new RetryProperties();

    public RateLimitProperties getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitProperties rateLimit) {
        this.rateLimit = rateLimit;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    public RetryProperties getRetry() {
        return retry;
    }

    public void setRetry(RetryProperties retry) {
        this.retry = retry;
    }

    public static class RateLimitProperties {
        private boolean enabled = true;
        private Map<CodingPlatform, PlatformLimit> platforms = new EnumMap<>(CodingPlatform.class);

        public RateLimitProperties() {
            // Default rate limits per platform
            platforms.put(CodingPlatform.CODEFORCES, new PlatformLimit(5, 2.0));
            platforms.put(CodingPlatform.GITHUB, new PlatformLimit(10, 0.5));
            platforms.put(CodingPlatform.LEETCODE, new PlatformLimit(5, 0.2));
            platforms.put(CodingPlatform.HACKERRANK, new PlatformLimit(5, 0.5));
            platforms.put(CodingPlatform.ATCODER, new PlatformLimit(5, 0.5));
            platforms.put(CodingPlatform.CODECHEF, new PlatformLimit(1, 0.1));
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<CodingPlatform, PlatformLimit> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(Map<CodingPlatform, PlatformLimit> platforms) {
            this.platforms = platforms;
        }

        public PlatformLimit getLimitFor(CodingPlatform platform) {
            return platforms.getOrDefault(platform, new PlatformLimit(5, 1.0));
        }
    }

    public static class PlatformLimit {
        private int capacity = 5;
        private double refillRatePerSecond = 1.0;

        public PlatformLimit() {
        }

        public PlatformLimit(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public double getRefillRatePerSecond() {
            return refillRatePerSecond;
        }

        public void setRefillRatePerSecond(double refillRatePerSecond) {
            this.refillRatePerSecond = refillRatePerSecond;
        }
    }

    public static class CacheProperties {
        private boolean enabled = true;
        private long ttlSeconds = 900L; // 15 minutes default
        private int maxEntries = 1000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public int getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }
    }

    public static class RetryProperties {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private long initialBackoffMs = 500L;
        private long maxBackoffMs = 5000L;
        private double jitterFactor = 0.2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialBackoffMs() {
            return initialBackoffMs;
        }

        public void setInitialBackoffMs(long initialBackoffMs) {
            this.initialBackoffMs = initialBackoffMs;
        }

        public long getMaxBackoffMs() {
            return maxBackoffMs;
        }

        public void setMaxBackoffMs(long maxBackoffMs) {
            this.maxBackoffMs = maxBackoffMs;
        }

        public double getJitterFactor() {
            return jitterFactor;
        }

        public void setJitterFactor(double jitterFactor) {
            this.jitterFactor = jitterFactor;
        }
    }
}
