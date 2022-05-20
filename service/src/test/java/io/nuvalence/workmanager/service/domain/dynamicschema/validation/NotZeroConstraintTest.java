package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotZeroConstraintTest {
    private Constraint<BigDecimal> constraint;

    @BeforeEach
    void setup() {
        constraint = new NotZeroConstraint();
    }

    @Test
    void isValid() {
        assertTrue(constraint.isValid(new BigDecimal(17.50)));
        assertTrue(constraint.isValid(new BigDecimal(20)));
        assertFalse(constraint.isValid(new BigDecimal(0)));
    }
}