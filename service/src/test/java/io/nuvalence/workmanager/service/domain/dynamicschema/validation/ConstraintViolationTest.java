package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintViolationTest {

    @Test
    void getMessage() {
        final ConstraintViolation violation = ConstraintViolation.builder()
                .path("password")
                .value("foo")
                .args(List.of(8, 20))
                .messageTemplate("{0} must be between {2} and {3} characters in length")
                .build();

        assertEquals("password must be between 8 and 20 characters in length", violation.getMessage());
    }
}