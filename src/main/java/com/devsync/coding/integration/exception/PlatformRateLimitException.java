package com.devsync.coding.integration.exception;

public class PlatformRateLimitException extends PlatformIntegrationException {

    private final Long retryAfterSeconds;

    public PlatformRateLimitException(String message) {
        this(message, (Long) null);
    }

    public PlatformRateLimitException(String message, Long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public PlatformRateLimitException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public PlatformRateLimitException(String message, Long retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
