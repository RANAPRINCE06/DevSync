package com.devsync.coding.enums;

/**
 * Declares granular platform capabilities supported by DevSync's Coding Intelligence module.
 * Allows UI and synchronization engine to dynamically reflect support levels without hardcoding
 * platform names across multiple presentation layers.
 */
public enum PlatformCapability {

    /**
     * Platform supports linking and navigating to the public developer/competitor profile URL.
     */
    PROFILE_LINK_SUPPORTED,

    /**
     * Platform supports automated background and on-demand synchronization of statistics.
     */
    AUTOMATED_SYNC_SUPPORTED,

    /**
     * Platform requires registered enterprise/partner OAuth2 credentials to synchronize.
     */
    AUTHENTICATED_SYNC_REQUIRED,

    /**
     * Platform statistics are provided via a verified community-maintained API (e.g. Kenkoooo AtCoder Problems).
     */
    COMMUNITY_API_SUPPORTED,

    /**
     * Platform provides competitive programming contest ratings and contest participation metrics.
     */
    CONTEST_RATING_SUPPORTED,

    /**
     * Platform exposes total accepted problems solved count.
     */
    PROBLEM_COUNTS_SUPPORTED,

    /**
     * Platform breaks down solved problems into difficulty tiers (e.g., Easy, Medium, Hard).
     */
    DIFFICULTY_BREAKDOWN_SUPPORTED,

    /**
     * Platform provides historical submission, contribution, or activity telemetry.
     */
    ACTIVITY_HISTORY_SUPPORTED
}
