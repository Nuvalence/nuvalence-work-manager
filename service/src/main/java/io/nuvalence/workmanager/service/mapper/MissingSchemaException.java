package io.nuvalence.workmanager.service.mapper;

import javax.validation.constraints.NotNull;

/**
 * Failure when a referenced schema cannot be retrieved.
 */
public class MissingSchemaException extends Exception {
    public MissingSchemaException(@NotNull final String schema) {
        super(String.format("Schema with name [%s] not found.", schema));
    }
}
