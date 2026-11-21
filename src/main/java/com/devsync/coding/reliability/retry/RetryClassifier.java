package com.devsync.coding.reliability.retry;

import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUnsupportedException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

/**
 * Classifies exceptions encountered during external platform integration as either
 * transient (retry eligible) or permanent (non-retryable).
 */
@Component
public class RetryClassifier {

    /**
     * Determines whether the given throwable represents a transient, retryable failure.
     *
     * @param throwable exception to inspect
     * @return true if retryable, false if permanent
     */
    public boolean isRetryable(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        // Permanent: 404 user not found
        if (throwable instanceof PlatformUserNotFoundException) {
            return false;
        }

        // Permanent: platform integration is unsupported or requires OAuth partner credentials
        if (throwable instanceof PlatformUnsupportedException) {
            return false;
        }

        // Permanent: validation or illegal argument errors
        if (throwable instanceof IllegalArgumentException) {
            return false;
        }

        // Transient: rate limit exceeded (HTTP 429 or X-RateLimit-Remaining: 0)
        if (throwable instanceof PlatformRateLimitException) {
            return true;
        }

        // Transient: network timeout or connectivity loss
        if (throwable instanceof ResourceAccessException) {
            return true;
        }

        // Check message contents for HTTP status codes
        String msg = throwable.getMessage() != null ? throwable.getMessage().toLowerCase() : "";

        // Permanent: client errors (400, 401, 403 authorization failure)
        if (msg.contains("http 400") || msg.contains("http 401") ||
                (msg.contains("http 403") && !msg.contains("rate limit"))) {
            return false;
        }

        // Transient: server errors (500, 502, 503, 504) or timeout
        if (msg.contains("500") || msg.contains("502") ||
                msg.contains("503") || msg.contains("504") ||
                msg.contains("bad gateway") || msg.contains("gateway timeout") ||
                msg.contains("service unavailable") || msg.contains("server unavailable") ||
                msg.contains("timed out") || msg.contains("timeout") ||
                msg.contains("connection refused") || msg.contains("connection reset") ||
                msg.contains("unreachable")) {
            return true;
        }

        // Check nested cause
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            return isRetryable(throwable.getCause());
        }

        // Fallback for generic integration exception: only retry if cause is transient
        return false;
    }
}
