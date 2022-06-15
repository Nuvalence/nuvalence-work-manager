package io.nuvalence.workmanager.service.usermanagementapi;

import io.nuvalence.workmanager.service.usermanagementapi.models.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Client to interface with User Management API.
 */
@Component
@Slf4j
public class UserManagementClient {

    @Value("${userManagement.baseUrl}")
    private String baseUrl;

    private RestTemplate httpClient;

    public UserManagementClient() {
        httpClient = new RestTemplate();
    }

    /**
     * Sets the class-level httpClient (to be used for mocking).
     * @param restTemplate The RestTemplate object
     */
    public void setHttpClient(RestTemplate restTemplate) {
        this.httpClient = restTemplate;
    }

    /**
     * Gets a user by email from the User Management API.
     * @param email email address to get user by.
     * @param token token from authorization header to pass to User Management API.
     * @param resource optional resource to retrieve permissions for associated application.
     * @return The user with given email address.
     */
    public Optional<User> getUserByEmail(String email, String token, String resource) {
        resource = ensureResourceNameIsInitialized(resource, "transaction_manager");

        ResponseEntity<User> response =
                getJson("/api/v2/user/email/" + email + "?resource=" + resource,
                        User.class, token);

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody());
        }

        return Optional.empty();
    }

    public Optional<User> getUserByEmail(String email, String token) {
        return this.getUserByEmail(email, token, null);
    }

    /**
     * Gets a user by id from the User Management API.
     * @param userId id to get user by.
     * @param token token from authorization header to pass to User Management API.
     * @param resource optional resource to retrieve permissions for associated application.
     * @return The user with the given id.
     */
    public Optional<User> getUserById(String userId, String token, String resource) {
        resource = ensureResourceNameIsInitialized(resource, "transaction_manager");
        ResponseEntity<User> response =
                getJson("/api/v2/user/" + userId + "?resource=" + resource,
                        User.class, token);

        if (response.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(response.getBody());
        }

        return Optional.empty();
    }

    public Optional<User> getUserById(String userId, String token) {
        return this.getUserById(userId, token, null);
    }

    private String ensureResourceNameIsInitialized(String resourceName, String defaultResourceName) {
        if (resourceName == null || resourceName.length() == 0) {
            return defaultResourceName;
        }

        return resourceName;
    }

    private <T> ResponseEntity<T> getJson(String endpoint, Class<T> responseType, String token) {
        final HttpEntity<?> headers = new HttpEntity<>(getHeaders(token));
        String url = baseUrl + endpoint;
        return httpClient.exchange(url, HttpMethod.GET, headers, responseType);
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!StringUtils.isEmpty(token)) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}
