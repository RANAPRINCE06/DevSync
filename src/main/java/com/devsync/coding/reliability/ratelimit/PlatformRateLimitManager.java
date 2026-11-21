package com.devsync.coding.reliability.ratelimit;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.reliability.config.CodingReliabilityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Manages per-platform token buckets ensuring external platform rate limits are respected.
 */
@Component
public class PlatformRateLimitManager {

    private static final Logger log = LoggerFactory.getLogger(PlatformRateLimitManager.class);

    private final CodingReliabilityProperties reliabilityProperties;
    private final Map<CodingPlatform, TokenBucket> buckets = new EnumMap<>(CodingPlatform.class);

    public PlatformRateLimitManager(CodingReliabilityProperties reliabilityProperties) {
        this.reliabilityProperties = reliabilityProperties;
        initBuckets();
    }

    private void initBuckets() {
        for (CodingPlatform platform : CodingPlatform.values()) {
            CodingReliabilityProperties.PlatformLimit limit = reliabilityProperties.getRateLimit().getLimitFor(platform);
            buckets.put(platform, new TokenBucket(limit.getCapacity(), limit.getRefillRatePerSecond()));
        }
    }

    /**
     * Attempts to acquire 1 permit for the given platform.
     *
     * @param platform platform to query
     * @return true if permitted, false if rate limited
     */
    public boolean tryAcquire(CodingPlatform platform) {
        if (!reliabilityProperties.getRateLimit().isEnabled()) {
            return true;
        }

        TokenBucket bucket = buckets.get(platform);
        if (bucket == null) {
            return true;
        }

        boolean acquired = bucket.tryAcquire();
        if (!acquired) {
            log.warn("Rate limit exceeded for platform [{}] (availableTokens={})",
                    platform, bucket.getAvailableTokens());
        }
        return acquired;
    }

    /**
     * Returns the estimated number of seconds before the next token is available.
     */
    public long getEstimatedWaitTimeSeconds(CodingPlatform platform) {
        TokenBucket bucket = buckets.get(platform);
        return bucket != null ? bucket.getWaitTimeSeconds() : 0L;
    }

    /**
     * Resets the token bucket for a platform (useful for testing or manual resets).
     */
    public void reset(CodingPlatform platform) {
        TokenBucket bucket = buckets.get(platform);
        if (bucket != null) {
            bucket.reset();
        }
    }
}
