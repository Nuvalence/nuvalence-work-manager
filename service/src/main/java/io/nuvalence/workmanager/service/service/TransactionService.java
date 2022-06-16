package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.TransactionCountByStatusModel;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.models.TransactionFilters;
import io.nuvalence.workmanager.service.repository.TransactionRepository;
import io.nuvalence.workmanager.service.specifications.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

/**
 * Service layer to manage transactions.
 */
@Component
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class TransactionService {
    private final TransactionRepository repository;
    private final TransactionFactory factory;
    private final TransactionTaskService transactionTaskService;
    private final EntityService entityService;
    private final WorkflowTasksService workflowTasksService;

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
        return this.createTransaction(definition, null);
    }

    /**
     * Create a new transaction for a given transaction definition.
     *
     * @param definition Type of transaction to create
     * @param jwt JSON Web Token from HTTP request
     * @return The newly created transaction
     * @throws MissingSchemaException if the transaction definition references a schema that does not exist
     * @throws MissingEntityException if the entity cannot be loaded post-creation
     */
    public Transaction createTransaction(final TransactionDefinition definition, String jwt)
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
     * Looks up a transaction by processInstanceID.
     *
     * @param processInstanceId ID of the process instance
     * @return Optional wrapping transaction
     * @throws MissingEntityException If transaction references an entity that does not exist
     */
    public Optional<Transaction> getTransactionByProcessInstanceId(String processInstanceId)
            throws MissingEntityException {
        final Optional<Transaction> optional = repository.findByProcessInstanceId(processInstanceId);

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
     * @param condition optional condition passed that influences decisions in workflow
     * @throws MissingTaskException If the process instance for this transaction does not have a task matching taskId
     * @throws JsonProcessingException If the data could not be serialized to JSON
     */
    public void completeTask(final Transaction transaction, final String taskId, final String condition)
            throws MissingTaskException, JsonProcessingException {
        transactionTaskService.completeTask(transaction, taskId, condition);
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
     * Gets a list of filtered transactions.
     *
     * @param filters What to filter/sort the transactions by
     * @return List of transactions
     * @throws MissingEntityException If any of the transactions returned reference missing entities
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public Page<Transaction> getFilteredTransactions(final TransactionFilters filters)
            throws MissingEntityException {
        Sort sort = Sort.unsorted();

        if (StringUtils.isNotBlank(filters.getSortCol())) {
            switch (filters.getSortCol().toLowerCase()) {
                // TODO: These are the only valid sorts so far, replace later when columns are more defined
                case "priority":
                case "id":
                case "district":
                case "createdtimestamp":
                case "lastupdatedtimestamp":
                case "status":
                    if (filters.getSortDir().equalsIgnoreCase("desc")) {
                        sort = Sort.by(Sort.Direction.DESC, filters.getSortCol());
                    } else {
                        sort = Sort.by(Sort.Direction.ASC, filters.getSortCol());
                    }
                    break;
                default:
                    break;
            }
        }

        Map<String, List<String>> statusMap = workflowTasksService.getStatusMap(
                filters.getCategory(),
                filters.getTransactionDefinitionKey()
        );

        filters.setStatus(getInternalStatusesFromPublicStatusList(filters.getStatus(), statusMap));

        Page<Transaction> transactions = repository.findAll(
                new TransactionSpecification().getTransactions(filters),
                PageRequest.of(
                        filters.getPageNumber() != null ? filters.getPageNumber() : 0,
                        filters.getPageSize() != null ? filters.getPageSize() : 25,
                        sort)
        );

        for (Transaction t : transactions) {
            t.loadEntity(entityService);
        }

        return transactions;
    }

    /**
     * Get list of statuses with a count of how many transactions have each status.
     *
     * @param filters What to filter the transactions by
     * @return List of statuses and counts of transactions per status
     */
    public List<TransactionCountByStatusModel> getTransactionCountsByStatus(final TransactionFilters filters) {
        List<String> statuses = workflowTasksService
                .getCamundaStatuses(WorkflowTasksService.StatusType.PUBLIC.name(),
                        filters.getCategory(), filters.getTransactionDefinitionKey());

        Map<String, List<String>> statusMap = workflowTasksService.getStatusMap(
                filters.getCategory(),
                filters.getTransactionDefinitionKey()
        );

        filters.setStatus(getInternalStatusesFromPublicStatusList(filters.getStatus(), statusMap));

        List<TransactionCountByStatusModel> counts = repository
                .getTransactionCountsByStatus(new TransactionSpecification().getTransactions(filters));

        // map the internal status to the public status
        counts.forEach(c -> {
            for (Map.Entry<String, List<String>> entry : statusMap.entrySet()) {
                if (entry.getValue().stream().anyMatch(internalStatus ->
                        internalStatus.equalsIgnoreCase(c.getStatus()))) {
                    c.setStatus(entry.getKey());
                }
            }
        });

        statuses.forEach(s -> {
            // if a status filter is passed in, only iterate through those statuses being filtered on
            if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
                if (filters.getStatus().stream().noneMatch(fs -> fs.equalsIgnoreCase(s))) {
                    return;
                }
            }

            Optional<TransactionCountByStatusModel> foundStatus = counts
                    .stream().filter(c -> c.getStatus().equalsIgnoreCase(s))
                    .findFirst();

            // if status is not found, add it anyway since we'll need to know the count (which will be 0)
            if (foundStatus.isEmpty()) {
                TransactionCountByStatusModel count = new TransactionCountByStatusModel();
                count.setCount(0);
                count.setStatus(s);
                counts.add(count);
            }
        });

        // since statuses could potentially be repeated (since multiple internal statuses can have the same public
        // status) we will need to group them
        Map<String, TransactionCountByStatusModel> groupedCounts = new HashMap<>();
        counts.forEach(c -> {
            if (!groupedCounts.containsKey(c.getStatus())) {
                TransactionCountByStatusModel count = new TransactionCountByStatusModel();
                count.setStatus(c.getStatus());
                count.setCount(0);
                groupedCounts.put(c.getStatus(), count);
            }

            Integer currentCount = groupedCounts.get(c.getStatus()).getCount();
            groupedCounts.get(c.getStatus()).setCount(currentCount + c.getCount());
        });

        // sort by status then return
        return groupedCounts.values()
                .stream().sorted(Comparator.comparing(s -> s.getStatus().toLowerCase()))
                .collect(Collectors.toList());
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

    /**
     * Gets a list of all transactions for a given transaction definition.
     *
     * @param userId User key to identify transactions
     * @return List of transactions
     * @throws MissingEntityException If any of the transactions returned reference missing entities
     */
    public List<Transaction> getTransactionsByAssignee(String userId)
            throws MissingEntityException {
        List<Transaction> results = new ArrayList<>();
        for (Transaction transaction : repository.searchByTransactionByAssignee(userId)) {
            transaction.loadEntity(entityService);
            results.add(transaction);
        }
        return results;
    }

    private List<String> getInternalStatusesFromPublicStatusList(List<String> statuses,
                                                                 Map<String, List<String>> statusMap) {
        if (statuses == null || statuses.isEmpty()) {
            return statuses;
        }

        // this assumes the statuses being passed in are public
        List<String> internalStatuses = new ArrayList<>();
        statuses.forEach(s -> {
            if (statusMap.containsKey(s)) {
                internalStatuses.addAll(statusMap.get(s));
            }
        });

        // add the "public" statuses (could have been internal statuses instead)
        internalStatuses.addAll(statuses);

        return internalStatuses;
    }
}
