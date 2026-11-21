package com.devsync.coding.integration.exception;

public class PlatformUserNotFoundException extends PlatformIntegrationException {

    public PlatformUserNotFoundException(String message) {
        super(message);
    }

    public PlatformUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
