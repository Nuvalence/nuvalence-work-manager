package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Factory that encapsulates transaction initialization logic.
 */
@Component
@RequiredArgsConstructor
public class TransactionFactory {
    private final ProcessEngine processEngine;
    private final EntityService entityService;
    private final SchemaService schemaService;

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
        final ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(definition.getProcessDefinitionKey());
        final Schema schema = schemaService.getSchemaByName(definition.getEntitySchema())
                .orElseThrow(() -> new MissingSchemaException(definition.getEntitySchema()));
        final Entity entity = entityService.saveEntity(new Entity(schema));
        final OffsetDateTime now = OffsetDateTime.now(clock);
        final Transaction transaction = Transaction.builder()
                .transactionDefinitionId(definition.getId())
                .transactionDefinitionKey(definition.getKey())
                .processInstanceId(processInstance.getId())
                .entityId(entity.getId())
                .status(definition.getDefaultStatus())
                .priority("medium") // default to medium TODO: check with FE dropdown to ingest that
                .createdBy("Dummy user") //TODO: set this to the user id once login is implemented
                .createdTimestamp(now)
                .lastUpdatedTimestamp(now)
                .build();
        transaction.loadEntity(entityService);

        return transaction;
    }
}
