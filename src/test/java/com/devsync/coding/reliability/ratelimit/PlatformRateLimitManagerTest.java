package com.devsync.coding.reliability.ratelimit;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.reliability.config.CodingReliabilityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlatformRateLimitManagerTest — Unit Tests for Token Bucket Rate Limiting")
class PlatformRateLimitManagerTest {

    private CodingReliabilityProperties properties;
    private PlatformRateLimitManager rateLimitManager;

    @BeforeEach
    void setUp() {
        properties = new CodingReliabilityProperties();
        // Configure specific limits for testing
        properties.getRateLimit().setEnabled(true);
        properties.getRateLimit().getPlatforms().put(CodingPlatform.CODEFORCES,
                new CodingReliabilityProperties.PlatformLimit(3, 10.0));
        properties.getRateLimit().getPlatforms().put(CodingPlatform.GITHUB,
                new CodingReliabilityProperties.PlatformLimit(2, 1.0));

        rateLimitManager = new PlatformRateLimitManager(properties);
    }

    @Test
    @DisplayName("tryAcquire — Allows requests up to configured bucket capacity")
    void tryAcquire_WithinCapacity_ReturnsTrue() {
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.CODEFORCES));
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.CODEFORCES));
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.CODEFORCES));
    }

    @Test
    @DisplayName("tryAcquire — Rejects requests when capacity is exhausted")
    void tryAcquire_ExhaustedCapacity_ReturnsFalse() {
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.GITHUB));
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.GITHUB));
        assertFalse(rateLimitManager.tryAcquire(CodingPlatform.GITHUB));
        assertTrue(rateLimitManager.getEstimatedWaitTimeSeconds(CodingPlatform.GITHUB) > 0);
    }

    @Test
    @DisplayName("tryAcquire — Platform limits are isolated and independent")
    void tryAcquire_DifferentPlatforms_AreIndependent() {
        // Exhaust GitHub
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.GITHUB));
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.GITHUB));
        assertFalse(rateLimitManager.tryAcquire(CodingPlatform.GITHUB));

        // Codeforces should still have full capacity
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.CODEFORCES));
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.CODEFORCES));
        assertTrue(rateLimitManager.tryAcquire(CodingPlatform.CODEFORCES));
    }

    @Test
    @DisplayName("tryAcquire — Disabled rate limiting always permits requests")
    void tryAcquire_Disabled_AlwaysReturnsTrue() {
        properties.getRateLimit().setEnabled(false);
        PlatformRateLimitManager disabledManager = new PlatformRateLimitManager(properties);

        for (int i = 0; i < 20; i++) {
            assertTrue(disabledManager.tryAcquire(CodingPlatform.GITHUB));
        }
        assertEquals(0, disabledManager.getEstimatedWaitTimeSeconds(CodingPlatform.GITHUB));
    }

    @Test
    @DisplayName("TokenBucket — Refill over time replenishes tokens")
    void tokenBucket_Refill_ReplenishesTokens() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(1, 20.0); // 20 tokens/sec
        assertTrue(bucket.tryAcquire(1));
        assertFalse(bucket.tryAcquire(1));

        Thread.sleep(100); // 100ms should replenish ~2 tokens
        assertTrue(bucket.tryAcquire(1));
    }
}
