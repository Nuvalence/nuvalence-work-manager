package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Serializable persistence model for attribute definitions.
 */
@Data
public class SchemaAttributeJson {
    private String name;
    private String type;
    private String contentType;
    private String entitySchema;
    private List<ConstraintJson> constraints = new LinkedList<>();
}
