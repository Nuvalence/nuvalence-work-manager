package io.nuvalence.workmanager.service.domain.transaction;

import java.util.UUID;

/**
 * Failure when a referenced entity cannot be retrieved.
 */
public class MissingEntityException extends Exception {
    public MissingEntityException(UUID entityId) {
        super("Transaction references non-existent entity with ID: " + entityId.toString());
    }
}
