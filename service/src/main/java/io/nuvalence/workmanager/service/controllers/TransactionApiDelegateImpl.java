package io.nuvalence.workmanager.service.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.auth.WorkerToken;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTransactionDefinitionException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.domain.transaction.TransactionLink;
import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkNotAllowedException;
import io.nuvalence.workmanager.service.generated.controllers.TransactionApiDelegate;
import io.nuvalence.workmanager.service.generated.models.LinkedTransaction;
import io.nuvalence.workmanager.service.generated.models.PagedTransactionModel;
import io.nuvalence.workmanager.service.generated.models.TransactionCreationRequest;
import io.nuvalence.workmanager.service.generated.models.TransactionLinkCreationRequest;
import io.nuvalence.workmanager.service.generated.models.TransactionLinkModel;
import io.nuvalence.workmanager.service.generated.models.TransactionModel;
import io.nuvalence.workmanager.service.generated.models.TransactionUpdateRequest;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.mapper.OffsetDateTimeMapper;
import io.nuvalence.workmanager.service.mapper.TransactionLinkMapper;
import io.nuvalence.workmanager.service.mapper.TransactionMapper;
import io.nuvalence.workmanager.service.models.TransactionFilters;
import io.nuvalence.workmanager.service.service.TransactionDefinitionService;
import io.nuvalence.workmanager.service.service.TransactionLinkService;
import io.nuvalence.workmanager.service.service.TransactionService;
import io.nuvalence.workmanager.service.service.WorkflowTasksService;
import io.nuvalence.workmanager.service.usermanagementapi.UserManagementClient;
import io.nuvalence.workmanager.service.usermanagementapi.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.ForbiddenException;

/**
 * Controller layer for Transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class TransactionApiDelegateImpl implements TransactionApiDelegate {
    private final TransactionService service;
    private final TransactionMapper mapper;
    private final TransactionDefinitionService transactionDefinitionService;
    private final TransactionLinkService transactionLinkService;
    private final WorkflowTasksService workflowTasksService;
    private final EntityMapper entityMapper;
    private final UserManagementClient userManagementClient;
    private Map<String, User> users = new HashMap<>();

    @Override
    public ResponseEntity<TransactionModel> getTransaction(UUID id)  {
        final Optional<TransactionModel> entity;
        try {
            entity = service.getTransactionById(id)
                    .map(t -> createTransactionModel(t));
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                if (authentication instanceof WorkerToken) {
                    String createdByEmail = ((WorkerToken) authentication).getUserEmail();
                    Optional<User> user = userManagementClient.getUserByEmail(createdByEmail,
                            ((WorkerToken) authentication).getOriginalToken());

                    return ResponseEntity.ok(service.getTransactionsByUser(user.get().getId().toString())
                            .stream()
                            .map(t -> createTransactionModel(t))
                            .collect(Collectors.toList()));
                }
            }
            throw new ForbiddenException();
        } catch (MissingEntityException e) {
            log.error("One or more transactions reference missing entities.", e);
            return ResponseEntity.status(500).build();
        } catch (ForbiddenException e) {
            log.error("Security context authentication not set.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Override
    public ResponseEntity<List<TransactionModel>> getTransactions(String transactionDefinitionKey) {
        try {
            return ResponseEntity.ok(
                    service.getTransactionsForDefinition(transactionDefinitionKey)
                            .stream()
                            .map(t -> createTransactionModel(t))
                            .collect(Collectors.toList())
            );
        } catch (MissingEntityException e) {
            log.error("One or more transactions reference missing entities.", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<PagedTransactionModel> getFilteredTransactions(String transactionDefinitionKey,
                                                                         String category,
                                                                         String startDate,
                                                                         String endDate,
                                                                         String priority,
                                                                         String status,
                                                                         String sortCol,
                                                                         String sortDir,
                                                                         Integer pageNumber,
                                                                         Integer pageSize) {
        try {
            TransactionFilters filters = TransactionFilters.builder()
                    .transactionDefinitionKey(transactionDefinitionKey)
                    .category(category)
                    .startDate(OffsetDateTimeMapper.INSTANCE.toOffsetDateTimeStartOfDay(startDate))
                    .endDate(OffsetDateTimeMapper.INSTANCE.toOffsetDateTimeEndOfDay(endDate))
                    .priority(priority)
                    .status(status)
                    .sortCol(sortCol)
                    .sortDir(sortDir)
                    .pageNumber(pageNumber)
                    .pageSize(pageSize)
                    .build();

            Page<TransactionModel> results = service.getFilteredTransactions(filters)
                    .map(mapper::transactionToTransactionModel);

            PagedTransactionModel model = new PagedTransactionModel();
            model.totalPages(results.getTotalPages());
            model.totalCount(BigDecimal.valueOf(results.getTotalElements()));
            model.items(results.toList());

            return ResponseEntity.ok(model);
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
                            .map(t -> createTransactionModel(t))
                            .collect(Collectors.toList())
            );
        } catch (MissingEntityException e) {
            log.error("One or more transactions reference missing entities.", e);
            return ResponseEntity.status(500).build();
        }
    }

    @Override
    public ResponseEntity<TransactionModel> postTransaction(TransactionCreationRequest request,
        String authorization) {
        try {
            final TransactionDefinition definition = transactionDefinitionService
                    .getTransactionDefinitionByKey(request.getTransactionDefinitionKey())
                    .orElseThrow(
                            () -> new MissingTransactionDefinitionException(request.getTransactionDefinitionKey())
                    );
            final Transaction transaction = service.createTransaction(definition, authorization);
            return ResponseEntity.ok(createTransactionModel(transaction));
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
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
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

            if (StringUtils.isNotEmpty(request.getAssignedTo())) {
                transaction.setAssignedTo(request.getAssignedTo());
            } else if ("".equals(request.getAssignedTo())) {
                transaction.setAssignedTo("");
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
                String condition = request.getCondition() != null ? request.getCondition() : "";
                service.completeTask(transaction, taskId, condition);
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
            } catch (JsonProcessingException e) {
                log.error(
                        String.format(
                                "Unable to save data for task with key [%s] in transaction with ID %s",
                                taskId,
                                transaction.getId()
                        ),
                        e
                );
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY.value()).build();
            }
        }

        return ResponseEntity.ok(createTransactionModel(updated));
    }

    @Override
    public ResponseEntity<TransactionLinkModel> linkTransactions(TransactionLinkCreationRequest request) {
        try {
            final TransactionLink transactionLink = transactionLinkService.saveTransactionLink(
                    TransactionLinkMapper.INSTANCE.transactionLinkRequestToTransactionLink(request),
                    request.getTransactionLinkTypeId()
            );

            return ResponseEntity
                    .status(201)
                    .body(
                        TransactionLinkMapper.INSTANCE.transactionLinkToTransactionLinkModel(transactionLink)
                    );
        } catch (MissingEntityException e) {
            log.error("transaction references missing entity.");
            return ResponseEntity.status(424).build();
        } catch (MissingTransactionDefinitionException e) {
            log.error("transaction contains an entity with missing schema(s).");
            return ResponseEntity.status(424).build();
        } catch (TransactionLinkNotAllowedException e) {
            log.error("transactions not allowed to be linked.");
            return ResponseEntity.status(400).build();
        }
    }

    @Override
    public ResponseEntity<List<LinkedTransaction>> getLinkedTransactionsById(UUID id) {
        final List<LinkedTransaction> results = transactionLinkService.getLinkedTransactionsById(id);
        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<List<String>> getAvailableStatuses(String type, String category, String key) {
        final List<String> results = workflowTasksService.getCamundaStatuses(type, category, key);
        return ResponseEntity.status(200).body(results);
    }

    private TransactionModel createTransactionModel(Transaction t) {
        TransactionModel transactionModel = mapper.transactionToTransactionModel(t);
        Optional<User> user = getUserByIdFromCache(t.getCreatedBy());
        transactionModel.setCreatedByDisplayName(user.get().getDisplayName());
        // TODO: Once we create a way to submit a transaction on behalf of a different user, we will need to get this
        //  user by subjectUserId, but subjectUserId and createdBy should always be equal for now. This will also
        //  need to be updated in TransactionFactory.java
        Optional<User> subjectUser = user;
        transactionModel.setSubjectUserDisplayName(subjectUser.get().getDisplayName());
        return transactionModel;
    }

    private Optional<User> getUserByIdFromCache(String id) {
        if (users.containsKey(id)) {
            return Optional.ofNullable(users.get(id));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userManagementClient.getUserById(id,
                ((WorkerToken) authentication).getOriginalToken());
        users.put(id, user.get());
        return user;
    }
}
