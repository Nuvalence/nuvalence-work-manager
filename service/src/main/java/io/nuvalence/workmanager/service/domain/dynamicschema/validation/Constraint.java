package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * A single validation constraint that can be applied to an entity property.
 *
 * @param <T> Property type this constraint applies to
 */
@EqualsAndHashCode
public abstract class Constraint<T> {

    public abstract Class<T> getType();

    public abstract String getMessageTemplate();

    public abstract Collection<Object> getArgs();

    public abstract boolean isValid(final T value);

    /**
     * Applies validation to a value, returning true if the value conforms to this constraint. Returns false and adds a
     * {@link ConstraintViolation} to the {@link ValidationContext} if the value does not confirm to the constraint.
     *
     * @param value value to apply constraint to
     * @param context context holding the overall results of an entity validation
     * @return true if validation passes, false otherwise
     */
    public final boolean isValid(final T value, final ValidationContext context) {
        final boolean valid = isValid(value);
        if (!valid) {
            context.add(
                    ConstraintViolation.builder()
                            .path(context.getPath())
                            .messageTemplate(getMessageTemplate())
                            .value(value)
                            .args(getArgs())
                            .build()
            );
        }

        return valid;
    }
}
