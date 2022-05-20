package io.nuvalence.workmanager.service.domain.transaction;

/**
 * Failure when a referenced transaction definition cannot be retrieved.
 */
public class MissingTransactionDefinitionException extends Exception {
    public MissingTransactionDefinitionException(String transactionDefinitionKey) {
        super("Transaction references non-existent definition with Key: " + transactionDefinitionKey);
    }
}
