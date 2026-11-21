package com.devsync.coding.reliability.cache;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.reliability.config.CodingReliabilityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlatformProfileCacheTest — Unit Tests for External Profile Cache")
class PlatformProfileCacheTest {

    private CodingReliabilityProperties properties;
    private PlatformProfileCache cache;

    @BeforeEach
    void setUp() {
        properties = new CodingReliabilityProperties();
        properties.getCache().setEnabled(true);
        properties.getCache().setTtlSeconds(2L); // 2 seconds TTL for tests
        properties.getCache().setMaxEntries(3);
        cache = new PlatformProfileCache(properties);
    }

    @Test
    @DisplayName("put and get — Returns cached data and handles case-insensitive usernames")
    void putAndGet_ReturnsCachedData() {
        ExternalProfileData data = ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username("AliceDev")
                .problemsSolved(250)
                .fetchedAt(Instant.now())
                .build();

        cache.put(CodingPlatform.LEETCODE, "AliceDev", data);

        ExternalProfileData cached = cache.get(CodingPlatform.LEETCODE, "alicedev");
        assertNotNull(cached);
        assertEquals(250, cached.getProblemsSolved());
    }

    @Test
    @DisplayName("evict — Removes entry from cache")
    void evict_RemovesEntry() {
        ExternalProfileData data = ExternalProfileData.builder()
                .platform(CodingPlatform.CODEFORCES)
                .username("tourist")
                .currentRating(3800.0)
                .fetchedAt(Instant.now())
                .build();

        cache.put(CodingPlatform.CODEFORCES, "tourist", data);
        assertNotNull(cache.get(CodingPlatform.CODEFORCES, "tourist"));

        cache.evict(CodingPlatform.CODEFORCES, "tourist");
        assertNull(cache.get(CodingPlatform.CODEFORCES, "tourist"));
    }

    @Test
    @DisplayName("get — Expired entry is evicted upon access")
    void get_ExpiredEntry_ReturnsNull() throws InterruptedException {
        ExternalProfileData data = ExternalProfileData.builder()
                .platform(CodingPlatform.GITHUB)
                .username("octocat")
                .fetchedAt(Instant.now())
                .build();

        cache.put(CodingPlatform.GITHUB, "octocat", data);
        assertNotNull(cache.get(CodingPlatform.GITHUB, "octocat"));

        Thread.sleep(2200); // Wait for 2s TTL to expire
        assertNull(cache.get(CodingPlatform.GITHUB, "octocat"));
    }

    @Test
    @DisplayName("put — Enforces maxEntries bounds")
    void put_ExceedingMaxEntries_EnforcesBounds() {
        for (int i = 0; i < 5; i++) {
            ExternalProfileData data = ExternalProfileData.builder()
                    .platform(CodingPlatform.GITHUB)
                    .username("user" + i)
                    .fetchedAt(Instant.now())
                    .build();
            cache.put(CodingPlatform.GITHUB, "user" + i, data);
        }

        assertTrue(cache.size() <= 3, "Cache size must not exceed maxEntries");
    }

    @Test
    @DisplayName("disabled — Does not store or return entries")
    void disabled_DoesNotCache() {
        properties.getCache().setEnabled(false);
        PlatformProfileCache disabledCache = new PlatformProfileCache(properties);

        ExternalProfileData data = ExternalProfileData.builder()
                .platform(CodingPlatform.GITHUB)
                .username("octocat")
                .fetchedAt(Instant.now())
                .build();

        disabledCache.put(CodingPlatform.GITHUB, "octocat", data);
        assertNull(disabledCache.get(CodingPlatform.GITHUB, "octocat"));
        assertEquals(0, disabledCache.size());
    }
}
