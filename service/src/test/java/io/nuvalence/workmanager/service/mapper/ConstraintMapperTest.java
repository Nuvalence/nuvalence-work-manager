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
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintMapperTest {

    @Test
    void correctlyMapsNotNullConstraints() {
        final Constraint<?> constraint = new NotNullConstraint();
        final ValidationConstraintModel model = new NotNullConstraintModel().constraintType("NotNull");
        final ConstraintJson json = new NotNullConstraintJson();

        assertEquals(constraint, ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(model));
        assertEquals(model, ConstraintMapper.INSTANCE.constraintToValidationConstraintModel(constraint));
        assertEquals(constraint, ConstraintMapper.INSTANCE.constraintJsonToConstraint(json));
        assertEquals(json, ConstraintMapper.INSTANCE.constraintToConstraintJson(constraint));
        assertNull(ConstraintMapper.INSTANCE.toModel((NotNullConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((NotNullConstraintModel) null));
        assertNull(ConstraintMapper.INSTANCE.toJson((NotNullConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((NotNullConstraintJson) null));
    }

    @Test
    void correctlyMapsNotBlankConstraints() {
        final Constraint<?> constraint = new NotBlankConstraint();
        final ValidationConstraintModel model = new NotBlankConstraintModel().constraintType("NotBlank");
        final ConstraintJson json = new NotBlankConstraintJson();

        assertEquals(constraint, ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(model));
        assertEquals(model, ConstraintMapper.INSTANCE.constraintToValidationConstraintModel(constraint));
        assertEquals(constraint, ConstraintMapper.INSTANCE.constraintJsonToConstraint(json));
        assertEquals(json, ConstraintMapper.INSTANCE.constraintToConstraintJson(constraint));
        assertNull(ConstraintMapper.INSTANCE.toModel((NotBlankConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((NotBlankConstraintModel) null));
        assertNull(ConstraintMapper.INSTANCE.toJson((NotBlankConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((NotBlankConstraintJson) null));
    }

    @Test
    void correctlyMapsNotZeroConstraints() {
        final Constraint<?> constraint = new NotZeroConstraint();
        final ValidationConstraintModel model = new NotZeroConstraintModel().constraintType("NotZero");
        final ConstraintJson json = new NotZeroConstraintJson();

        assertEquals(constraint, ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(model));
        assertEquals(model, ConstraintMapper.INSTANCE.constraintToValidationConstraintModel(constraint));
        assertEquals(constraint, ConstraintMapper.INSTANCE.constraintJsonToConstraint(json));
        assertEquals(json, ConstraintMapper.INSTANCE.constraintToConstraintJson(constraint));
        assertNull(ConstraintMapper.INSTANCE.toModel((NotZeroConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((NotZeroConstraintModel) null));
        assertNull(ConstraintMapper.INSTANCE.toJson((NotZeroConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((NotZeroConstraintJson) null));
    }

    @Test
    void correctlyMapsMonetaryConstraints() {
        final Constraint<?> constraint = new MonetaryConstraint();
        final ValidationConstraintModel model = new MonetaryConstraintModel().constraintType("Monetary");
        final ConstraintJson json = new MonetaryConstraintJson();

        assertEquals(constraint, ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(model));
        assertEquals(model, ConstraintMapper.INSTANCE.constraintToValidationConstraintModel(constraint));
        assertEquals(constraint, ConstraintMapper.INSTANCE.constraintJsonToConstraint(json));
        assertEquals(json, ConstraintMapper.INSTANCE.constraintToConstraintJson(constraint));
        assertNull(ConstraintMapper.INSTANCE.toModel((MonetaryConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((MonetaryConstraintModel) null));
        assertNull(ConstraintMapper.INSTANCE.toJson((MonetaryConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((MonetaryConstraintJson) null));
    }

    @Test
    void correctlyMapsLengthConstraints() {
        final Constraint<?> constraint = LengthConstraint.builder()
                .min(5)
                .max(10)
                .build();
        final ValidationConstraintModel model = new LengthConstraintModel()
                .min(5)
                .max(10)
                .constraintType("Length");
        final ConstraintJson json = LengthConstraintJson.builder()
                .min(5)
                .max(10)
                .build();

        assertEquals(constraint, ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(model));
        assertEquals(model, ConstraintMapper.INSTANCE.constraintToValidationConstraintModel(constraint));
        assertEquals(constraint, ConstraintMapper.INSTANCE.constraintJsonToConstraint(json));
        assertEquals(json, ConstraintMapper.INSTANCE.constraintToConstraintJson(constraint));
        assertNull(ConstraintMapper.INSTANCE.toModel((LengthConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((LengthConstraintModel) null));
        assertNull(ConstraintMapper.INSTANCE.toJson((LengthConstraint) null));
        assertNull(ConstraintMapper.INSTANCE.toDomain((LengthConstraintJson) null));
    }

    @Test
    void validationConstraintModelToConstraintReturnsNullForUnmappedConstraintModelTypes() {
        assertNull(ConstraintMapper.INSTANCE.constraintToValidationConstraintModel(new UnmappableConstraint()));
    }

    @Test
    void constraintToValidationConstraintModelReturnsNullForUnmappedConstraintTypes() {
        assertNull(ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(new UnmappableConstraintModel()));
    }

    @Test
    void constraintToConstraintJsonReturnsNullForUnmappedConstraintModelTypes() {
        assertNull(ConstraintMapper.INSTANCE.constraintToConstraintJson(new UnmappableConstraint()));
    }

    @Test
    void constraintJsonToConstraintReturnsNullForUnmappedConstraintTypes() {
        assertNull(ConstraintMapper.INSTANCE.constraintJsonToConstraint(new UnmappableConstraintJson()));
    }

    private static final class UnmappableConstraint extends Constraint<String> {

        @Override
        public Class<String> getType() {
            return null;
        }

        @Override
        public String getMessageTemplate() {
            return null;
        }

        @Override
        public Collection<Object> getArgs() {
            return null;
        }

        @Override
        public boolean isValid(String value) {
            return false;
        }
    }

    private static final class UnmappableConstraintModel extends ValidationConstraintModel {
        private UnmappableConstraintModel() {
            setConstraintType("unmappable");
        }
    }

    private static final class UnmappableConstraintJson extends ConstraintJson {

    }
}