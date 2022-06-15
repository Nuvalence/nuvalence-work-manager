package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.auth.WorkerToken;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.usermanagementapi.UserManagementClient;
import io.nuvalence.workmanager.service.usermanagementapi.models.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Factory that encapsulates transaction initialization logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class TransactionFactory {
    private final ProcessEngine processEngine;
    private final EntityService entityService;
    private final SchemaService schemaService;
    private final UserManagementClient userManagementClient;

    @Setter(AccessLevel.PACKAGE)
    private Clock clock = Clock.systemDefaultZone();

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
        String createdByEmail = "dummyUser@email.com";
        UUID createdByUserId = new UUID(0L, 0L);
        UUID subjectUserId = new UUID(0L, 0L);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication instanceof WorkerToken) {
                createdByEmail = ((WorkerToken) authentication).getUserEmail();
                Optional<User> user = userManagementClient.getUserByEmail(createdByEmail,
                        ((WorkerToken) authentication).getOriginalToken());
                createdByUserId = user.get().getId();
                // TODO: When we start allowing users to create transactions on behalf of another user and determine how
                //  to actually link the subject, we will need to update this with the real subjectUserId. For now,
                //  it is just populated by the user that is creating the transaction. This will also need to be
                //  fixed in TransactionApiDelegateImpl.java.
                subjectUserId = user.get().getId();
            }
        }


        final ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(definition.getProcessDefinitionKey());
        final Schema schema = schemaService.getSchemaByName(definition.getEntitySchema())
                .orElseThrow(() -> new MissingSchemaException(definition.getEntitySchema()));
        final Entity entity = entityService.saveEntity(new Entity(schema));
        final OffsetDateTime now = OffsetDateTime.now(clock);
        // get the status from the execution entity set by the sequence flow listener
        String status;
        try {
            status = (String) ((ProcessInstanceWithVariablesImpl) processInstance)
                    .getExecutionEntity().getVariable("status");
            if (status == null) {
                status = definition.getDefaultStatus();
            }
        } catch (Exception e) {
            log.warn("Status not set on initial workflow instance; defaulting.");
            status = definition.getDefaultStatus();
        }
        final Transaction transaction = Transaction.builder()
                .transactionDefinitionId(definition.getId())
                .transactionDefinitionKey(definition.getKey())
                .processInstanceId(processInstance.getId())
                .entityId(entity.getId())
                .status(status)
                .priority("medium") // default to medium TODO: check with FE dropdown to ingest that
                .createdBy(createdByUserId != new UUID(0L, 0L) ? createdByUserId.toString() : createdByEmail)
                .subjectUserId(subjectUserId != new UUID(0L, 0L) ? subjectUserId.toString() : createdByEmail)
                .assignedTo("") // TODO: We should determine if a default assignee is set
                .createdTimestamp(now)
                .lastUpdatedTimestamp(now)
                .build();
        transaction.loadEntity(entityService);

        return transaction;
    }
}
