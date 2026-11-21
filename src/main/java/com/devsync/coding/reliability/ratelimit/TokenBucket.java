package com.devsync.coding.reliability.ratelimit;

/**
 * Thread-safe token bucket rate limiter implementation.
 * Tokens accumulate continuously based on a refill rate up to maximum capacity.
 */
public class TokenBucket {

    private final int capacity;
    private final double refillRatePerSecond;
    private double availableTokens;
    private long lastRefillNanos;

    public TokenBucket(int capacity, double refillRatePerSecond) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Token bucket capacity must be positive");
        }
        if (refillRatePerSecond <= 0) {
            throw new IllegalArgumentException("Refill rate must be positive");
        }
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.availableTokens = capacity;
        this.lastRefillNanos = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        return tryAcquire(1);
    }

    public synchronized boolean tryAcquire(int tokens) {
        refill();
        if (availableTokens >= tokens) {
            availableTokens -= tokens;
            return true;
        }
        return false;
    }

    public synchronized double getAvailableTokens() {
        refill();
        return availableTokens;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getRefillRatePerSecond() {
        return refillRatePerSecond;
    }

    public synchronized long getWaitTimeSeconds() {
        refill();
        if (availableTokens >= 1.0) {
            return 0L;
        }
        double needed = 1.0 - availableTokens;
        return Math.max(1L, (long) Math.ceil(needed / refillRatePerSecond));
    }

    public synchronized void reset() {
        this.availableTokens = capacity;
        this.lastRefillNanos = System.nanoTime();
    }

    private void refill() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
        if (elapsedSeconds > 0) {
            availableTokens = Math.min(capacity, availableTokens + (elapsedSeconds * refillRatePerSecond));
            lastRefillNanos = now;
        }
    }
}
