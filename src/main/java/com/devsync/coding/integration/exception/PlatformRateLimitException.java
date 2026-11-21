package com.devsync.coding.integration.exception;

public class PlatformRateLimitException extends PlatformIntegrationException {

    public PlatformRateLimitException(String message) {
        super(message);
    }

    public PlatformRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
