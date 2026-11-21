package com.devsync.coding.reliability.retry;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.reliability.config.CodingReliabilityProperties;
import com.devsync.coding.reliability.metrics.CodingSyncMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Executes platform operations with exponential backoff, jitter, and Retry-After header inspection.
 */
@Component
public class PlatformSyncRetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(PlatformSyncRetryExecutor.class);

    private final CodingReliabilityProperties reliabilityProperties;
    private final RetryClassifier retryClassifier;
    private final CodingSyncMetrics codingSyncMetrics;

    public PlatformSyncRetryExecutor(CodingReliabilityProperties reliabilityProperties,
                                     RetryClassifier retryClassifier,
                                     CodingSyncMetrics codingSyncMetrics) {
        this.reliabilityProperties = reliabilityProperties;
        this.retryClassifier = retryClassifier;
        this.codingSyncMetrics = codingSyncMetrics;
    }

    /**
     * Executes a platform retrieval task with configured retry and backoff policy.
     *
     * @param platform target coding platform
     * @param username username / handle being queried
     * @param task operation to execute
     * @param <T> result type
     * @return operation result
     * @throws Exception if all retries are exhausted or a non-retryable error occurs
     */
    public <T> T executeWithRetry(CodingPlatform platform, String username, Callable<T> task) throws Exception {
        CodingReliabilityProperties.RetryProperties retryProps = reliabilityProperties.getRetry();
        if (!retryProps.isEnabled()) {
            return task.call();
        }

        int maxAttempts = Math.max(1, retryProps.getMaxAttempts());
        int attempt = 0;

        while (true) {
            attempt++;
            try {
                return task.call();
            } catch (Exception ex) {
                boolean retryable = retryClassifier.isRetryable(ex);
                if (attempt >= maxAttempts || !retryable) {
                    if (!retryable) {
                        log.debug("Non-retryable error for [{}:{}] on attempt {}: {}",
                                platform, username, attempt, ex.getMessage());
                    } else {
                        log.warn("Exhausted all {} retry attempt(s) for [{}:{}]: {}",
                                maxAttempts, platform, username, ex.getMessage());
                    }
                    throw ex;
                }

                long delayMs = calculateDelay(retryProps, attempt, ex, platform);
                log.warn("Transient failure querying [{}:{}], attempt {}/{}. Retrying in {}ms: {}",
                        platform, username, attempt, maxAttempts, delayMs, ex.getMessage());

                codingSyncMetrics.recordRetry(platform);

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PlatformIntegrationException("Retry interrupted for platform " + platform, ie);
                }
            }
        }
    }

    private long calculateDelay(CodingReliabilityProperties.RetryProperties props, int attempt, Exception ex, CodingPlatform platform) throws Exception {
        if (ex instanceof PlatformRateLimitException rateLimitEx && rateLimitEx.getRetryAfterSeconds() != null) {
            long retryAfterMs = rateLimitEx.getRetryAfterSeconds() * 1000L;
            if (retryAfterMs > props.getMaxBackoffMs()) {
                // If Retry-After exceeds max backoff, fail fast to avoid blocking background threads
                log.warn("Platform [{}] Retry-After ({}s) exceeds max allowed backoff ({}s); aborting retries",
                        platform, rateLimitEx.getRetryAfterSeconds(), props.getMaxBackoffMs() / 1000);
                throw ex;
            }
            return Math.max(100L, retryAfterMs);
        }

        long backoff = props.getInitialBackoffMs() * (1L << (attempt - 1));
        backoff = Math.min(backoff, props.getMaxBackoffMs());

        double jitter = props.getJitterFactor();
        if (jitter > 0) {
            double randomMultiplier = 1.0 + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * jitter;
            backoff = (long) (backoff * randomMultiplier);
        }

        return Math.max(10L, backoff);
    }
}
