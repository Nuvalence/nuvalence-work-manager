package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.Arrays;
import java.util.Collection;

/**
 * Validates that the String value under inspection is between an optional minimum length and an optional maximum
 * length.
 */
@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class LengthConstraint extends Constraint<CharSequence> {
    @Getter
    Class<CharSequence> type = CharSequence.class;

    Integer min;
    Integer max;

    @Override
    public String getMessageTemplate() {
        if (min != null && min.equals(max)) {
            return "{0} must be exactly {2} characters in length";
        } else if (min != null && max != null) {
            return "{0} must be between {2} and {3} characters in length";
        } else if (min != null) {
            return "{0} must be at least {2} characters in length";
        } else if (max != null) {
            return "{0} must be at most {3} characters in length";
        }

        return "{0} can be any length";
    }

    @Override
    public Collection<Object> getArgs() {
        return Arrays.asList(min, max);
    }

    @Override
    public boolean isValid(final CharSequence value) {
        final CharSequence valueToTest = (value == null) ? "" : value;

        return (min == null || valueToTest.length() >= min) && (max == null || valueToTest.length() <= max);
    }
}
