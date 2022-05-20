package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

/**
 * Validates that the value under inspection is not null.
 */
@EqualsAndHashCode(callSuper = true)
public final class NotNullConstraint extends Constraint<Object> {

    @Getter
    private final Class<Object> type = Object.class;

    @Getter
    private final String messageTemplate = "{0} must not be null";

    @Getter
    private final Collection<Object> args = Collections.emptyList();

    @Override
    public boolean isValid(Object value) {
        return value != null;
    }
}
