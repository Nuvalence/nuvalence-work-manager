package io.nuvalence.workmanager.service.auth;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

/**
 * Custom class to send Exceptions.
 */
@Data
public class TokenSecurityExceptionWrapper {
    private final String message;
    private final HttpStatus httpStatus;
    private final ZonedDateTime zonedDateTime;
}
