package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.repository.TransactionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
/**
 * Service layer to manage transaction definitions.
 */

@Component
@Transactional
@RequiredArgsConstructor
public class TransactionDefinitionService {
    private final TransactionDefinitionRepository repository;

    /**
     * Fetches a transaction definition from the database by id (primary key).
     *
     * @param id transaction definition id to fetch
     * @return transaction definition object
     */
    public Optional<TransactionDefinition> getTransactionDefinitionById(final UUID id) {
        return repository.findById(id);
    }

    /**
     * Fetches the latest version of a transaction definition from the database by key.
     *
     * @param key transaction definition key to fetch
     * @return transaction definition object
     */
    public Optional<TransactionDefinition> getTransactionDefinitionByKey(final String key) {
        // TODO When we implement versioned transaction configuration, this will need to select for the newest version
        return repository.searchByKey(key).stream().findFirst();
    }

    /**
     * Returns a list of transaction definitions whose names match the query passed in.
     *
     * @param name Partial name query
     * @return List of transaction definitions matching query
     */
    public List<TransactionDefinition> getTransactionDefinitionsByPartialNameMatch(final String name) {
        if (name == null) {
            return repository.getAllTransactions();
        } else {
            return repository.searchByPartialName(name);
        }
    }

    /**
     * Returns a list of transaction definitions whose names match the query passed in.
     *
     * @param category Partial name query
     * @return List of transaction definitions matching query
     */
    public List<TransactionDefinition> getTransactionDefinitionsByPartialCategoryMatch(final String category) {
        if (category == null) {
            return repository.getAllTransactions();
        } else {
            return repository.searchByPartialCategory(category);
        }
    }

    /**
     * Saves a transaction definition.
     *
     * @param transactionDefinition transaction definition to save
     * @return post-save version of transaction definition
     */
    public TransactionDefinition saveTransactionDefinition(final TransactionDefinition transactionDefinition) {
        return repository.save(transactionDefinition);
    }
}
