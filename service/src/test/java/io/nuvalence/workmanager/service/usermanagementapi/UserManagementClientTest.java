package io.nuvalence.workmanager.service.usermanagementapi;

import io.nuvalence.workmanager.service.usermanagementapi.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserManagementClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserManagementClient client;

    @BeforeEach
    void initTests() {
        client.setHttpClient(restTemplate);
    }

    @Test
    public void getUserByEmail() {
        when(restTemplate.exchange(ArgumentMatchers.anyString(), eq(HttpMethod.GET),
                ArgumentMatchers.any(),
                eq(User.class)))
                .thenReturn(getUserResponse());

        Optional<User> user = client.getUserByEmail("email", "token", "resource");

        assertEquals(user.get().getEmail(), "someEmail@something.com");
    }

    @Test
    public void getUserByEmail_NoResourceName() {
        when(restTemplate.exchange(ArgumentMatchers.anyString(), eq(HttpMethod.GET),
                ArgumentMatchers.any(),
                eq(User.class)))
                .thenReturn(getUserResponse());

        Optional<User> user = client.getUserByEmail("email", "token");

        assertEquals(user.get().getEmail(), "someEmail@something.com");
    }

    private ResponseEntity<User> getUserResponse() {
        User response = User.builder()
                                .email("someEmail@something.com")
                                .id(UUID.randomUUID())
                                .build();

        return ResponseEntity.ok(response);
    }
}
