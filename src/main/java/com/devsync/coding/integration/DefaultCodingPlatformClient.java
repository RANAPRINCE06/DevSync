package com.devsync.coding.integration;

import com.devsync.coding.integration.config.CodingIntegrationProperties;
import com.devsync.coding.integration.exception.PlatformIntegrationException;
import com.devsync.coding.integration.exception.PlatformRateLimitException;
import com.devsync.coding.integration.exception.PlatformUserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;

@Component
public class DefaultCodingPlatformClient implements CodingPlatformClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultCodingPlatformClient.class);
    private static final String DEFAULT_USER_AGENT = "DevSync-CodingIntelligence/1.0";

    private final RestClient restClient;

    @org.springframework.beans.factory.annotation.Autowired
    public DefaultCodingPlatformClient(CodingIntegrationProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT)
                .build();
    }

    public DefaultCodingPlatformClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public <T> T get(String url, Class<T> responseType, Map<String, String> headers) {
        try {
            RestClient.RequestHeadersSpec<?> spec = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON);

            if (headers != null) {
                headers.forEach(spec::header);
            }

            return spec.retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        int code = resp.getStatusCode().value();
                        if (code == 404) {
                            throw new PlatformUserNotFoundException("Profile not found at external URL: " + url);
                        } else if (code == 429) {
                            throw new PlatformRateLimitException("External platform rate limit reached (HTTP 429) for URL: " + url);
                        } else if (code == 403) {
                            String remaining = resp.getHeaders().getFirst("X-RateLimit-Remaining");
                            if ("0".equals(remaining)) {
                                throw new PlatformRateLimitException("GitHub API rate limit exhausted (HTTP 403, X-RateLimit-Remaining: 0)");
                            }
                            throw new PlatformIntegrationException("External platform access forbidden (HTTP 403)");
                        }
                        throw new PlatformIntegrationException("External platform client error: HTTP " + code);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new PlatformIntegrationException("External platform server unavailable: HTTP " + resp.getStatusCode().value());
                    })
                    .body(responseType);
        } catch (PlatformIntegrationException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.warn("External platform request timed out or network error: {}", ex.getMessage());
            throw new PlatformIntegrationException("External platform connection timed out or unreachable: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error querying external platform: {}", ex.getMessage());
            throw new PlatformIntegrationException("Failed to retrieve external platform profile: " + ex.getMessage(), ex);
        }
    }

    @Override
    public <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, String> headers) {
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (headers != null) {
                headers.forEach(spec::header);
            }

            return spec.body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        int code = resp.getStatusCode().value();
                        if (code == 404) {
                            throw new PlatformUserNotFoundException("Profile not found at external URL: " + url);
                        } else if (code == 429) {
                            throw new PlatformRateLimitException("External platform rate limit reached (HTTP 429)");
                        }
                        throw new PlatformIntegrationException("External platform client error: HTTP " + code);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new PlatformIntegrationException("External platform server unavailable: HTTP " + resp.getStatusCode().value());
                    })
                    .body(responseType);
        } catch (PlatformIntegrationException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.warn("External platform request timed out: {}", ex.getMessage());
            throw new PlatformIntegrationException("External platform connection timed out: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error posting to external platform: {}", ex.getMessage());
            throw new PlatformIntegrationException("Failed to query external platform: " + ex.getMessage(), ex);
        }
    }
}
