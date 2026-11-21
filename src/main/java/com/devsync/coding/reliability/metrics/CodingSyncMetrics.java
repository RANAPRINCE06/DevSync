package com.devsync.coding.reliability.metrics;

import com.devsync.coding.enums.CodingPlatform;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publishes operational and reliability metrics for the Coding Intelligence module
 * via Micrometer into Spring Boot Actuator (/actuator/metrics).
 * Excludes sensitive user details (user IDs, usernames, tokens) from metric dimensions.
 */
@Component
public class CodingSyncMetrics {

    private final MeterRegistry registry;

    @Autowired
    public CodingSyncMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordAttempt(CodingPlatform platform, String triggerType) {
        if (registry != null && platform != null) {
            registry.counter("coding.sync.attempts",
                    "platform", platform.name(),
                    "trigger", triggerType != null ? triggerType : "unknown"
            ).increment();
        }
    }

    public void recordSuccess(CodingPlatform platform) {
        if (registry != null && platform != null) {
            registry.counter("coding.sync.success", "platform", platform.name()).increment();
        }
    }

    public void recordFailure(CodingPlatform platform, String errorType) {
        if (registry != null && platform != null) {
            registry.counter("coding.sync.failure",
                    "platform", platform.name(),
                    "error_type", errorType != null ? errorType : "unknown"
            ).increment();
        }
    }

    public void recordRetry(CodingPlatform platform) {
        if (registry != null && platform != null) {
            registry.counter("coding.sync.retries", "platform", platform.name()).increment();
        }
    }

    public void recordRateLimited(CodingPlatform platform) {
        if (registry != null && platform != null) {
            registry.counter("coding.sync.rate_limited", "platform", platform.name()).increment();
        }
    }

    public void recordCacheHit(CodingPlatform platform) {
        if (registry != null && platform != null) {
            registry.counter("coding.cache.hits", "platform", platform.name()).increment();
        }
    }

    public void recordCacheMiss(CodingPlatform platform) {
        if (registry != null && platform != null) {
            registry.counter("coding.cache.misses", "platform", platform.name()).increment();
        }
    }

    public Timer.Sample startTimer() {
        return registry != null ? Timer.start(registry) : null;
    }

    public void stopTimer(Timer.Sample sample, CodingPlatform platform, String outcome) {
        if (registry != null && sample != null && platform != null) {
            sample.stop(registry.timer("coding.sync.duration",
                    "platform", platform.name(),
                    "outcome", outcome != null ? outcome : "unknown"
            ));
        }
    }
}
