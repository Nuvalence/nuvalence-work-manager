package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

/**
 * Serializable persistence model for LengthConstraints.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Jacksonized
public class LengthConstraintJson extends ConstraintJson {
    private Integer min;
    private Integer max;
}
