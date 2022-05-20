package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonetaryConstraintTest {
    private Constraint<BigDecimal> constraint;

    @BeforeEach
    void setup() {
        constraint = new MonetaryConstraint();
    }

    @Test
    void isValid() {
        assertTrue(constraint.isValid(new BigDecimal("17.50")));
        assertTrue(constraint.isValid(new BigDecimal(20)));
        assertTrue(constraint.isValid(new BigDecimal(0)));
        assertTrue(constraint.isValid(new BigDecimal("10.1")));
        assertTrue(constraint.isValid(null));
        assertFalse(constraint.isValid(new BigDecimal("15.55555")));
    }
}