package com.devsync.coding.integration.exception;

public class PlatformUnsupportedException extends PlatformIntegrationException {

    public PlatformUnsupportedException(String message) {
        super(message);
    }

    public PlatformUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
