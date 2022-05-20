package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;

/**
 * Serializable persistence base model for validation constraints.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "constraintType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NotNullConstraintJson.class, name = "NotNull"),
        @JsonSubTypes.Type(value = NotBlankConstraintJson.class, name = "NotBlank"),
        @JsonSubTypes.Type(value = LengthConstraintJson.class, name = "Length"),
        @JsonSubTypes.Type(value = NotZeroConstraintJson.class, name = "NotZero"),
        @JsonSubTypes.Type(value = MonetaryConstraintJson.class, name = "Monetary")
})
@EqualsAndHashCode
public class ConstraintJson {
}
