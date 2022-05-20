package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTransactionDefinitionException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.controllers.TransactionApiDelegate;
import io.nuvalence.workmanager.service.generated.models.TransactionCreationRequest;
import io.nuvalence.workmanager.service.generated.models.TransactionModel;
import io.nuvalence.workmanager.service.generated.models.TransactionUpdateRequest;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.mapper.TransactionMapper;
import io.nuvalence.workmanager.service.service.TransactionDefinitionService;
import io.nuvalence.workmanager.service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller layer for Transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionApiDelegateImpl implements TransactionApiDelegate {
    private final TransactionService service;
    private final TransactionMapper mapper;
    private final TransactionDefinitionService transactionDefinitionService;
    private final EntityMapper entityMapper;

    @Override
    public ResponseEntity<TransactionModel> getTransaction(UUID id)  {
        final Optional<TransactionModel> entity;
        try {
            entity = service.getTransactionById(id)
                    .map(mapper::transactionToTransactionModel);
        } catch (MissingEntityException e) {
            log.error(
                    String.format("Transaction [%s] references missing entity.", id),
                    e
            );
            return ResponseEntity.status(500).build();
        }
        return (entity.isEmpty())
                ? ResponseEntity.status(404).build()
                : ResponseEntity.status(200).body(entity.get());
    }

    @Override
    public ResponseEntity<List<TransactionModel>> getTransactionsByUser() {
        try {
            //TODO: Implement actual user information on filter instead of Dummy user
            String userId = "Dummy user";
            return ResponseEntity.ok(service.getTransactionsByUser(userId)
                    .stream()
                    .map(mapper::transactionToTransactionModel)
                    .collect(Collectors.toList()));
        } catch (MissingEntityException e) {
            log.error("One or more transactions reference missing entities.", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<List<TransactionModel>> getTransactions(String transactionDefinitionKey) {
        try {
            return ResponseEntity.ok(
                    service.getTransactionsForDefinition(transactionDefinitionKey)
                            .stream()
                            .map(mapper::transactionToTransactionModel)
                            .collect(Collectors.toList())
            );
        } catch (MissingEntityException e) {
            log.error("One or more transactions reference missing entities.", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<List<TransactionModel>> getTransactionsByCategory(String category) {
        try {
            return ResponseEntity.ok(
                    service.getTransactionsByCategory(category)
                            .stream()
                            .map(mapper::transactionToTransactionModel)
                            .collect(Collectors.toList())
            );
        } catch (MissingEntityException e) {
            log.error("One or more transactions reference missing entities.", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<TransactionModel> postTransaction(TransactionCreationRequest request) {
        try {
            final TransactionDefinition definition = transactionDefinitionService
                    .getTransactionDefinitionByKey(request.getTransactionDefinitionKey())
                    .orElseThrow(
                            () -> new MissingTransactionDefinitionException(request.getTransactionDefinitionKey())
                    );
            final Transaction transaction = service.createTransaction(definition);
            return ResponseEntity.ok(mapper.transactionToTransactionModel(transaction));
        } catch (MissingSchemaException e) {
            log.error(
                    String.format(
                            "transaction definition [%s] references missing schema.",
                            request.getTransactionDefinitionKey()
                    ),
                    e
            );
            return ResponseEntity.status(424).build();
        } catch (MissingTransactionDefinitionException e) {
            log.error(
                    String.format(
                            "ID [%s] references missing transaction definition.",
                            request.getTransactionDefinitionKey()
                    ),
                    e
            );
            return ResponseEntity.status(424).build();
        } catch (MissingEntityException e) {
            log.error("An error occurred initializing entity for this transaction", e);
            return ResponseEntity.status(424).build();
        }
    }

    @Override
    public ResponseEntity<TransactionModel> updateTransaction(UUID id,
                                                              TransactionUpdateRequest request,
                                                              String taskId) {
        final Transaction transaction;
        final Transaction updated;
        try {
            transaction = service.getTransactionById(id).orElse(null);
            if (transaction == null) {
                return ResponseEntity.notFound().build();
            }

            // check request for a valid priority then extract priority
            if (request.getPriority() != null
                    && request.getPriority().toLowerCase().matches("low|medium|high|urgent")) {
                transaction.setPriority(request.getPriority());
            }

            if (request.getStatus().toLowerCase().matches("new|edits|info|"
                    + "supervisor|super_approved|inspection|inspection_scheduled|approved|rejected|review")) {
                transaction.setStatus(request.getStatus());
            }

            entityMapper.applyMappedPropertiesToEntity(transaction.getData(), request.getData());
            updated = service.updateTransaction(transaction);
        } catch (MissingEntityException e) {
            log.error(String.format("transaction [%s] references missing entity.", id), e);
            return ResponseEntity.status(424).build();
        } catch (MissingSchemaException e) {
            log.error(String.format("transaction [%s] contains an entity with missing schema(s).", id), e);
            return ResponseEntity.status(424).build();
        }

        if (taskId != null) {
            try {
                service.completeTask(transaction, taskId);
            } catch (MissingTaskException e) {
                log.error(
                        String.format(
                                "Unable to find task with key [%s] in transaction with ID %s",
                                taskId,
                                transaction.getId()
                        ),
                        e
                );
                return ResponseEntity.status(424).build();
            }
        }

        return ResponseEntity.ok(mapper.transactionToTransactionModel(updated));
    }
}
