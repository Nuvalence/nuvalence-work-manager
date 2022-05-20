package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

/**
 * Service layer to manage transactions.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionFactory factory;
    private final TransactionTaskService transactionTaskService;
    private final EntityService entityService;

    /**
     * Create a new transaction for a given transaction definition.
     *
     * @param definition Type of transaction to create
     * @return The newly created transaction
     * @throws MissingSchemaException if the transaction definition references a schema that does not exist
     * @throws MissingEntityException if the entity cannot be loaded post-creation
     */
    public Transaction createTransaction(final TransactionDefinition definition)
            throws MissingSchemaException, MissingEntityException {
        return repository.save(factory.createTransaction(definition));
    }

    /**
     * Looks up a transaction by ID.
     *
     * @param id ID of transaction to find
     * @return Optional wrapping transaction
     * @throws MissingEntityException If transaction references an entity that does not exist
     */
    public Optional<Transaction> getTransactionById(final UUID id) throws MissingEntityException {
        final Optional<Transaction> optional = repository.findById(id);

        if (optional.isPresent()) {
            final Transaction transaction = optional.get();
            transaction.loadEntity(entityService);

            return Optional.of(transaction);
        }

        return Optional.empty();
    }

    /**
     * Updates the transaction in the database.
     *
     * @param transaction Transaction containing updated data.
     * @return Transaction post-update
     * @throws MissingEntityException If transaction references an entity that does not exist
     */
    public Transaction updateTransaction(final Transaction transaction) throws MissingEntityException {
        entityService.saveEntity(transaction.getData());
        repository.save(transaction);

        return getTransactionById(transaction.getId()).orElseThrow();
    }

    /**
     * Completes the given task, posting to the workflow the data in the transaction.
     *
     * @param transaction Transaction to complete task on
     * @param taskId ID of task to complete
     * @throws MissingTaskException If the process instance for this transaction does not have a task matching taskId
     */
    public void completeTask(final Transaction transaction, final String taskId) throws MissingTaskException {
        transactionTaskService.completeTask(transaction, taskId);
    }

    /**
     * Gets a list of all transactions for a given transaction definition.
     *
     * @param transactionDefinitionKey Key of transaction definition
     * @return List of transactions
     * @throws MissingEntityException If any of the transactions returned reference missing entities
     */
    public List<Transaction> getTransactionsForDefinition(final String transactionDefinitionKey)
            throws MissingEntityException {
        List<Transaction> results = new ArrayList<>();
        for (Transaction transaction : repository.searchByTransactionDefinitionKey(transactionDefinitionKey)) {
            transaction.loadEntity(entityService);
            results.add(transaction);
        }
        return results;
    }

    /**
     * Gets a list of all transactions for a given transaction by category.
     *
     * @param category Key of category you want to search by
     * @return List of transactions
     * @throws MissingEntityException If any of the transactions returned reference missing entities
     */
    public List<Transaction> getTransactionsByCategory(final String category)
            throws MissingEntityException {
        List<Transaction> results = new ArrayList<>();
        for (Transaction transaction : repository.searchByCategory(category)) {
            transaction.loadEntity(entityService);
            results.add(transaction);
        }
        return results;
    }

    /**
     * Gets a list of all transactions for a given transaction definition.
     *
     * @param userId User key to identify transactions
     * @return List of transactions
     * @throws MissingEntityException If any of the transactions returned reference missing entities
     */
    public List<Transaction> getTransactionsByUser(String userId)
            throws MissingEntityException {
        List<Transaction> results = new ArrayList<>();
        for (Transaction transaction : repository.searchByTransactionByUser(userId)) {
            transaction.loadEntity(entityService);
            results.add(transaction);
        }
        return results;
    }
}
