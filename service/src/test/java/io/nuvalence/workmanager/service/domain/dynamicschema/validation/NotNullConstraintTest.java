package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotNullConstraintTest {
    private Constraint<Object> constraint;

    @BeforeEach
    void setup() {
        constraint = new NotNullConstraint();
    }

    @Test
    void isValid() {
        assertTrue(constraint.isValid(new Object()));
        assertFalse(constraint.isValid(null));
    }
}