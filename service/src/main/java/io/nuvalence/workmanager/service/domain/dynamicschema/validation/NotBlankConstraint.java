package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

/**
 * Validates that the value under inspection is not null or an empty string after trimming leading and trailing
 * whitespace.
 */
@EqualsAndHashCode(callSuper = true)
public final class NotBlankConstraint extends Constraint<CharSequence> {
    @Getter
    private final Class<CharSequence> type = CharSequence.class;

    @Getter
    private final String messageTemplate = "{0} must not be blank";

    @Getter
    private final Collection<Object> args = Collections.emptyList();

    @Override
    public boolean isValid(CharSequence value) {
        if (value == null) {
            return false;
        }

        return value.toString().trim().length() > 0;
    }
}
