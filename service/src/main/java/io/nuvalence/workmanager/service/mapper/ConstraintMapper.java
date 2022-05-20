package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.ConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.LengthConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.MonetaryConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.NotBlankConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.NotNullConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.NotZeroConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.LengthConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.MonetaryConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotBlankConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotNullConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotZeroConstraint;
import io.nuvalence.workmanager.service.generated.models.LengthConstraintModel;
import io.nuvalence.workmanager.service.generated.models.MonetaryConstraintModel;
import io.nuvalence.workmanager.service.generated.models.NotBlankConstraintModel;
import io.nuvalence.workmanager.service.generated.models.NotNullConstraintModel;
import io.nuvalence.workmanager.service.generated.models.NotZeroConstraintModel;
import io.nuvalence.workmanager.service.generated.models.ValidationConstraintModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps validation constraints between the following 2 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.ValidationConstraintModel})</li>
 *     <li>Logic Object ({@link io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint})</li>
 * </ul>
 */
@Mapper
public interface ConstraintMapper {
    ConstraintMapper INSTANCE = Mappers.getMapper(ConstraintMapper.class);

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint} to
     * {@link io.nuvalence.workmanager.service.generated.models.ValidationConstraintModel}.
     *
     * @param constraint Logic model for validation constraint
     * @return API model for validation constraint
     */
    default ValidationConstraintModel constraintToValidationConstraintModel(final Constraint<?> constraint) {
        if (constraint instanceof NotNullConstraint) {
            return toModel((NotNullConstraint) constraint);
        } else if (constraint instanceof NotBlankConstraint) {
            return toModel((NotBlankConstraint) constraint);
        } else if (constraint instanceof LengthConstraint) {
            return toModel((LengthConstraint) constraint);
        } else if (constraint instanceof NotZeroConstraint) {
            return toModel((NotZeroConstraint) constraint);
        } else if (constraint instanceof MonetaryConstraint) {
            return toModel((MonetaryConstraint) constraint);
        } else {
            return null;
        }
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.generated.models.ValidationConstraintModel} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint}.
     *
     * @param model API model for validation constraint
     * @return Logic model for validation constraint
     */
    default Constraint<?> validationConstraintModelToConstraint(final ValidationConstraintModel model) {
        if (model instanceof NotNullConstraintModel) {
            return toDomain((NotNullConstraintModel) model);
        } else if (model instanceof NotBlankConstraintModel) {
            return toDomain((NotBlankConstraintModel) model);
        } else if (model instanceof LengthConstraintModel) {
            return toDomain((LengthConstraintModel) model);
        } else if (model instanceof NotZeroConstraintModel) {
            return toDomain((NotZeroConstraintModel) model);
        } else if (model instanceof MonetaryConstraintModel) {
            return toDomain((MonetaryConstraintModel) model);
        } else {
            return null;
        }
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.ConstraintJson}.
     *
     * @param constraint Logic model for validation constraint
     * @return JSON persistence model for validation constraint
     */
    default ConstraintJson constraintToConstraintJson(final Constraint<?> constraint) {
        if (constraint instanceof NotNullConstraint) {
            return toJson((NotNullConstraint) constraint);
        } else if (constraint instanceof NotBlankConstraint) {
            return toJson((NotBlankConstraint) constraint);
        } else if (constraint instanceof LengthConstraint) {
            return toJson((LengthConstraint) constraint);
        } else if (constraint instanceof NotZeroConstraint) {
            return toJson((NotZeroConstraint) constraint);
        } else if (constraint instanceof MonetaryConstraint) {
            return toJson((MonetaryConstraint) constraint);
        } else {
            return null;
        }
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.ConstraintJson} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint}.
     *
     * @param json JSON persistence model for validation constraint
     * @return Logic model for validation constraint
     */
    default Constraint<?> constraintJsonToConstraint(final ConstraintJson json) {
        if (json instanceof NotNullConstraintJson) {
            return toDomain((NotNullConstraintJson) json);
        } else if (json instanceof NotBlankConstraintJson) {
            return toDomain((NotBlankConstraintJson) json);
        } else if (json instanceof LengthConstraintJson) {
            return toDomain((LengthConstraintJson) json);
        } else if (json instanceof NotZeroConstraintJson) {
            return toDomain((NotZeroConstraintJson) json);
        } else if (json instanceof MonetaryConstraintJson) {
            return toDomain((MonetaryConstraintJson) json);
        } else {
            return null;
        }
    }


    @Mapping(target = "constraintType", constant = "NotNull")
    NotNullConstraintModel toModel(NotNullConstraint constraint);

    @Mapping(target = "constraintType", constant = "NotBlank")
    NotBlankConstraintModel toModel(NotBlankConstraint constraint);

    @Mapping(target = "constraintType", constant = "Length")
    LengthConstraintModel toModel(LengthConstraint constraint);

    @Mapping(target = "constraintType", constant = "NotZero")
    NotZeroConstraintModel toModel(NotZeroConstraint constraint);

    @Mapping(target = "constraintType", constant = "Monetary")
    MonetaryConstraintModel toModel(MonetaryConstraint constraint);

    NotNullConstraint toDomain(NotNullConstraintModel constraint);

    NotBlankConstraint toDomain(NotBlankConstraintModel constraint);

    LengthConstraint toDomain(LengthConstraintModel constraint);

    NotZeroConstraint toDomain(NotZeroConstraintModel constraint);

    MonetaryConstraint toDomain(MonetaryConstraintModel constraint);

    NotNullConstraint toDomain(NotNullConstraintJson constraint);

    NotBlankConstraint toDomain(NotBlankConstraintJson constraint);

    LengthConstraint toDomain(LengthConstraintJson constraint);

    NotZeroConstraint toDomain(NotZeroConstraintJson constraint);

    MonetaryConstraint toDomain(MonetaryConstraintJson constraint);

    NotNullConstraintJson toJson(NotNullConstraint constraint);

    NotBlankConstraintJson toJson(NotBlankConstraint constraint);

    LengthConstraintJson toJson(LengthConstraint constraint);

    NotZeroConstraintJson toJson(NotZeroConstraint constraint);

    MonetaryConstraintJson toJson(MonetaryConstraint constraint);
}
