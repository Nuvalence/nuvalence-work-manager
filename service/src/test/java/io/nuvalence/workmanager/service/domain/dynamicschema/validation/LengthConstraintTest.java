package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LengthConstraintTest {
    private Constraint<CharSequence> rangeConstraint;
    private Constraint<CharSequence> exactConstraint;
    private Constraint<CharSequence> minConstraint;
    private Constraint<CharSequence> maxConstraint;
    private Constraint<CharSequence> unenforcedConstraint;

    @BeforeEach
    void setup() {
        rangeConstraint = LengthConstraint.builder().min(1).max(5).build();
        exactConstraint = LengthConstraint.builder().min(5).max(5).build();
        minConstraint = LengthConstraint.builder().min(1).build();
        maxConstraint = LengthConstraint.builder().max(5).build();
        unenforcedConstraint = LengthConstraint.builder().build();
    }

    @Test
    void getMessageTemplate() {
        assertEquals("{0} must be between {2} and {3} characters in length", rangeConstraint.getMessageTemplate());
        assertEquals("{0} must be exactly {2} characters in length", exactConstraint.getMessageTemplate());
        assertEquals("{0} must be at least {2} characters in length", minConstraint.getMessageTemplate());
        assertEquals("{0} must be at most {3} characters in length", maxConstraint.getMessageTemplate());
        assertEquals("{0} can be any length", unenforcedConstraint.getMessageTemplate());
    }

    @Test
    void getArgs() {
        assertEquals(List.of(1, 5), rangeConstraint.getArgs());
        assertEquals(List.of(5, 5), exactConstraint.getArgs());
        assertEquals(Arrays.asList(1, (Object) null), minConstraint.getArgs());
        assertEquals(Arrays.asList((Object) null, 5), maxConstraint.getArgs());
        assertEquals(Arrays.asList((Object) null, (Object) null), unenforcedConstraint.getArgs());
    }

    @Test
    void isValid() {
        assertTrue(rangeConstraint.isValid("foo"));
        assertTrue(exactConstraint.isValid("foooo"));
        assertTrue(minConstraint.isValid("foo"));
        assertTrue(maxConstraint.isValid("foo"));
        assertTrue(unenforcedConstraint.isValid("foo"));

        assertFalse(rangeConstraint.isValid(null));
        assertFalse(rangeConstraint.isValid("foooooo"));
        assertFalse(rangeConstraint.isValid(""));
        assertFalse(exactConstraint.isValid("foo"));
        assertFalse(minConstraint.isValid(""));
        assertFalse(maxConstraint.isValid("foooooo"));

        assertFalse(rangeConstraint.isValid(null));
        assertFalse(exactConstraint.isValid(null));
        assertFalse(minConstraint.isValid(null));
        assertTrue(maxConstraint.isValid(null));
        assertTrue(unenforcedConstraint.isValid(null));
    }
}