package com.devsync.coding.reliability.retry;

import com.devsync.coding.enums.CodingPlatform;
import com.devsync.coding.integration.dto.ExternalProfileData;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import com.devsync.coding.reliability.config.CodingReliabilityProperties;
import com.devsync.coding.reliability.metrics.CodingSyncMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformSyncRetryExecutorTest — Unit Tests for Exponential Backoff and Retry Execution")
class PlatformSyncRetryExecutorTest {

    @Mock
    private CodingSyncMetrics metrics;

    private CodingReliabilityProperties properties;
    private RetryClassifier retryClassifier;
    private PlatformSyncRetryExecutor retryExecutor;

    @BeforeEach
    void setUp() {
        properties = new CodingReliabilityProperties();
        properties.getRetry().setEnabled(true);
        properties.getRetry().setMaxAttempts(3);
        properties.getRetry().setInitialBackoffMs(10L); // 10ms for fast tests
        properties.getRetry().setMaxBackoffMs(100L);
        properties.getRetry().setJitterFactor(0.0);

        retryClassifier = new RetryClassifier();
        retryExecutor = new PlatformSyncRetryExecutor(properties, retryClassifier, metrics);
    }

    @Test
    @DisplayName("executeWithRetry — Immediate success completes without retries")
    void executeWithRetry_ImmediateSuccess_NoRetries() throws Exception {
        ExternalProfileData expected = ExternalProfileData.builder()
                .platform(CodingPlatform.GITHUB)
                .username("test")
                .fetchedAt(Instant.now())
                .build();

        ExternalProfileData result = retryExecutor.executeWithRetry(
                CodingPlatform.GITHUB, "test", () -> expected
        );

        assertSame(expected, result);
        verifyNoInteractions(metrics);
    }

    @Test
    @DisplayName("executeWithRetry — Retries on transient failure and succeeds on 2nd attempt")
    void executeWithRetry_TransientFailure_SucceedsOnRetry() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        ExternalProfileData expected = ExternalProfileData.builder()
                .platform(CodingPlatform.LEETCODE)
                .username("test")
                .fetchedAt(Instant.now())
                .build();

        ExternalProfileData result = retryExecutor.executeWithRetry(CodingPlatform.LEETCODE, "test", () -> {
            if (callCount.incrementAndGet() == 1) {
                throw new PlatformIntegrationException("HTTP 503 Service Unavailable");
            }
            return expected;
        });

        assertEquals(2, callCount.get());
        assertSame(expected, result);
        verify(metrics).recordRetry(CodingPlatform.LEETCODE);
    }

    @Test
    @DisplayName("executeWithRetry — Exhausting max attempts throws original exception")
    void executeWithRetry_Exhaustion_ThrowsException() {
        AtomicInteger callCount = new AtomicInteger(0);

        PlatformIntegrationException thrown = assertThrows(PlatformIntegrationException.class, () ->
                retryExecutor.executeWithRetry(CodingPlatform.CODEFORCES, "tourist", () -> {
                    callCount.incrementAndGet();
                    throw new PlatformIntegrationException("Persistent 502 Bad Gateway");
                })
        );

        assertEquals(3, callCount.get());
        assertTrue(thrown.getMessage().contains("502 Bad Gateway"));
        verify(metrics, times(2)).recordRetry(CodingPlatform.CODEFORCES);
    }

    @Test
    @DisplayName("executeWithRetry — Non-transient failure (UserNotFound) fails fast without retry")
    void executeWithRetry_PermanentFailure_FailsFast() {
        AtomicInteger callCount = new AtomicInteger(0);

        assertThrows(PlatformUserNotFoundException.class, () ->
                retryExecutor.executeWithRetry(CodingPlatform.LEETCODE, "nonexistent", () -> {
                    callCount.incrementAndGet();
                    throw new PlatformUserNotFoundException("User nonexistent not found on LEETCODE");
                })
        );

        assertEquals(1, callCount.get(), "Must not retry permanent 404/not found errors");
        verifyNoInteractions(metrics);
    }

    @Test
    @DisplayName("executeWithRetry — Retry-After exceeding maxBackoff fails fast")
    void executeWithRetry_RetryAfterExceedsMax_FailsFast() {
        AtomicInteger callCount = new AtomicInteger(0);

        assertThrows(PlatformRateLimitException.class, () ->
                retryExecutor.executeWithRetry(CodingPlatform.GITHUB, "rate_limited_user", () -> {
                    callCount.incrementAndGet();
                    throw new PlatformRateLimitException("Rate limited on GITHUB", 3600L); // 1 hour wait
                })
        );

        assertEquals(1, callCount.get(), "Must not block thread for 1 hour; fails fast immediately");
    }
}
