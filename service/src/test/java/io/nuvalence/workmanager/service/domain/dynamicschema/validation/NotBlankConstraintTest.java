package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotBlankConstraintTest {
    private Constraint<CharSequence> constraint;

    @BeforeEach
    void setup() {
        constraint = new NotBlankConstraint();
    }

    @Test
    void isValid() {
        assertTrue(constraint.isValid("foo"));
        assertTrue(constraint.isValid("  foo  "));
        assertFalse(constraint.isValid(null));
        assertFalse(constraint.isValid(""));
        assertFalse(constraint.isValid("   "));
    }
}