package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Serializable persistence model for schemas.
 */
@Data
public class SchemaJson {
    private String name;
    private List<SchemaAttributeJson> attributes = new LinkedList<>();
}
