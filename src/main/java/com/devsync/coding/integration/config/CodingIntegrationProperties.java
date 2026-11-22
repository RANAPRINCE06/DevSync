package com.devsync.coding.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.coding.integration")
public class CodingIntegrationProperties {

    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;
    private PlatformConfig codeforces = new PlatformConfig("https://codeforces.com/api", true);
    private PlatformConfig github = new PlatformConfig("https://api.github.com", true);
    private PlatformConfig leetcode = new PlatformConfig("https://leetcode.com/graphql", true);
    private PlatformConfig codechef = new PlatformConfig("https://api.codechef.com", false);
    private PlatformConfig hackerrank = new PlatformConfig("https://www.hackerrank.com/api", false);
    private PlatformConfig atcoder = new PlatformConfig("https://kenkoooo.com/atcoder/atcoder-api/v3", true);

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public PlatformConfig getCodeforces() {
        return codeforces;
    }

    public void setCodeforces(PlatformConfig codeforces) {
        this.codeforces = codeforces;
    }

    public PlatformConfig getGithub() {
        return github;
    }

    public void setGithub(PlatformConfig github) {
        this.github = github;
    }

    public PlatformConfig getLeetcode() {
        return leetcode;
    }

    public void setLeetcode(PlatformConfig leetcode) {
        this.leetcode = leetcode;
    }

    public PlatformConfig getCodechef() {
        return codechef;
    }

    public void setCodechef(PlatformConfig codechef) {
        this.codechef = codechef;
    }

    public PlatformConfig getHackerrank() {
        return hackerrank;
    }

    public void setHackerrank(PlatformConfig hackerrank) {
        this.hackerrank = hackerrank;
    }

    public PlatformConfig getAtcoder() {
        return atcoder;
    }

    public void setAtcoder(PlatformConfig atcoder) {
        this.atcoder = atcoder;
    }

    public static class PlatformConfig {
        private String baseUrl;
        private boolean enabled;
        private String clientId;
        private String clientSecret;
        private String apiToken;

        public PlatformConfig() {
        }

        public PlatformConfig(String baseUrl, boolean enabled) {
            this.baseUrl = baseUrl;
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }
    }
}
