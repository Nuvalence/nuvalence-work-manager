package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {

    @Test
    void isValidAddsExpectedConstraintViolationToContext() {
        // Arrange
        final Constraint<CharSequence> constraint = LengthConstraint.builder()
                .min(8)
                .max(20)
                .build();
        final ValidationContext context = new ValidationContext();
        final ConstraintViolation expectedViolation = ConstraintViolation.builder()
                .path("password")
                .value("foo")
                .args(constraint.getArgs())
                .messageTemplate(constraint.getMessageTemplate())
                .build();

        // Act
        context.pushPath("password");
        constraint.isValid("foo", context);

        // Assert
        assertEquals(1, context.size());
        assertEquals(expectedViolation, context.get(0));
    }

    @Test
    void isValidDoesNotAddViolationsWhenValid() {
        // Arrange
        final Constraint<CharSequence> constraint = LengthConstraint.builder()
                .min(8)
                .max(20)
                .build();
        final ValidationContext context = new ValidationContext();

        // Act
        context.pushPath("password");
        constraint.isValid("MyPassword", context);

        // Assert
        assertEquals(0, context.size());
    }
}