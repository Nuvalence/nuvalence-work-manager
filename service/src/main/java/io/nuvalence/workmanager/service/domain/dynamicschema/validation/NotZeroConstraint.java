package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

/**
 * Validates that the value under inspection is not zero.
 */
@EqualsAndHashCode(callSuper = true)
public final class NotZeroConstraint extends Constraint<BigDecimal> {

    @Getter
    private final Class<BigDecimal> type = BigDecimal.class;

    @Getter
    private final String messageTemplate = "{0} must not be zero";

    @Getter
    private final Collection<Object> args = Collections.emptyList();

    @Override
    public boolean isValid(BigDecimal value) {
        return (value.compareTo(BigDecimal.ZERO) != 0);
    }
}
