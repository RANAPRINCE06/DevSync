package com.devsync.coding.reliability.cache;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.reliability.config.CodingReliabilityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory cache for recently retrieved external platform profile data.
 * Enforces configurable TTL expiration, capacity boundaries, and fine-grained invalidation.
 */
@Component
public class PlatformProfileCache {

    private static final Logger log = LoggerFactory.getLogger(PlatformProfileCache.class);

    private final CodingReliabilityProperties reliabilityProperties;
    private final Map<PlatformCacheKey, CacheEntry<ExternalProfileData>> cache = new ConcurrentHashMap<>();

    public PlatformProfileCache(CodingReliabilityProperties reliabilityProperties) {
        this.reliabilityProperties = reliabilityProperties;
    }

    public ExternalProfileData get(CodingPlatform platform, String username) {
        if (!isEnabled() || username == null) {
            return null;
        }

        PlatformCacheKey key = new PlatformCacheKey(platform, username);
        CacheEntry<ExternalProfileData> entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            log.debug("Cache entry expired for [{}:{}]", platform, username);
            return null;
        }

        log.debug("Cache hit for [{}:{}]", platform, username);
        return entry.getValue();
    }

    public void put(CodingPlatform platform, String username, ExternalProfileData data) {
        if (!isEnabled() || username == null || data == null) {
            return;
        }

        ensureCapacity();

        long ttlSeconds = reliabilityProperties.getCache().getTtlSeconds();
        Instant expiresAt = Instant.now().plus(Duration.ofSeconds(ttlSeconds));
        PlatformCacheKey key = new PlatformCacheKey(platform, username);

        cache.put(key, new CacheEntry<>(data, expiresAt));
        log.debug("Cached profile data for [{}:{}] (ttl={}s)", platform, username, ttlSeconds);
    }

    public void evict(CodingPlatform platform, String username) {
        if (username != null) {
            cache.remove(new PlatformCacheKey(platform, username));
            log.debug("Evicted cached profile for [{}:{}]", platform, username);
        }
    }

    public void clear() {
        cache.clear();
        log.debug("Cleared entire profile cache");
    }

    public int size() {
        cleanExpired();
        return cache.size();
    }

    public boolean isEnabled() {
        return reliabilityProperties.getCache().isEnabled();
    }

    private void ensureCapacity() {
        int maxEntries = reliabilityProperties.getCache().getMaxEntries();
        if (cache.size() >= maxEntries) {
            cleanExpired();
            if (cache.size() >= maxEntries) {
                // Evict the first entry found
                Iterator<PlatformCacheKey> iterator = cache.keySet().iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
            }
        }
    }

    private void cleanExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    public static record PlatformCacheKey(CodingPlatform platform, String username) {
        public PlatformCacheKey {
            username = username != null ? username.toLowerCase().trim() : "";
        }
    }

    public static class CacheEntry<T> {
        private final T value;
        private final Instant expiresAt;

        public CacheEntry(T value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        public T getValue() {
            return value;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public boolean isExpired() {
            return isExpired(Instant.now());
        }

        public boolean isExpired(Instant now) {
            return now.isAfter(expiresAt);
        }
    }
}
