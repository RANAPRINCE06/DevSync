package com.devsync.coding.integration;

import java.util.Map;

/**
 * HTTP client abstraction for communicating with external competitive programming
 * and developer platform APIs.
 */
public interface CodingPlatformClient {

    /**
     * Executes an HTTP GET request to the specified URL with optional custom headers.
     *
     * @param url          the target URL
     * @param responseType the expected response DTO type
     * @param headers      additional headers (e.g., User-Agent, Accept)
     * @param <T>          the response payload type
     * @return the deserialized response payload
     */
    <T> T get(String url, Class<T> responseType, Map<String, String> headers);

    /**
     * Executes an HTTP POST request to the specified URL with a JSON request body and headers.
     *
     * @param url          the target URL
     * @param requestBody  the payload object to serialize as JSON
     * @param responseType the expected response DTO type
     * @param headers      additional headers
     * @param <T>          the response payload type
     * @return the deserialized response payload
     */
    <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, String> headers);
}
