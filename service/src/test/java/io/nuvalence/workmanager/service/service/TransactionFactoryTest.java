package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TransactionFactoryTest {
    @Mock
    private ProcessEngine processEngine;

    @Mock
    private EntityService entityService;

    @Mock
    private SchemaService schemaService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private ProcessInstance processInstance;

    private TransactionFactory factory;
    private Clock clock;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        factory = new TransactionFactory(processEngine, entityService, schemaService);
        factory.setClock(clock);
    }

    @Test
    void createTransaction() throws MissingSchemaException, MissingEntityException {
        // Arrange
        final TransactionDefinition definition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("key")
                .name("Transaction")
                .defaultStatus("status")
                .entitySchema("schema")
                .category("")
                .processDefinitionKey("process-id")
                .build();
        final Schema schema = Schema.builder()
                .name("schema")
                .build();
        final UUID entityId = UUID.randomUUID();
        Mockito.when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito
                .when(runtimeService.startProcessInstanceByKey(definition.getProcessDefinitionKey()))
                .thenReturn(processInstance);
        Mockito.when(processInstance.getId()).thenReturn("process-instance-id");
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(entityService.saveEntity(new Entity(schema))).thenReturn(new Entity(schema, entityId));
        Mockito.when(entityService.getEntityById(entityId)).thenReturn(Optional.of(new Entity(schema, entityId)));
        final Transaction transaction = Transaction.builder()
                .transactionDefinitionId(definition.getId())
                .transactionDefinitionKey(definition.getKey())
                .processInstanceId("process-instance-id")
                .entityId(entityId)
                .status("status")
                .priority("medium")
                .createdBy("Dummy user") // TODO remove this stubbed value once integration with auth is implemented
                .createdTimestamp(OffsetDateTime.now(clock))
                .lastUpdatedTimestamp(OffsetDateTime.now(clock))
                .build();
        transaction.loadEntity(entityService);

        // Act and Assert
        assertEquals(transaction, factory.createTransaction(definition));
    }

    @Test
    void createTransactionThrowsMissingSchemaExceptionIfSchemaIsMissing() {
        // Arrange
        final TransactionDefinition definition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("key")
                .name("Transaction")
                .defaultStatus("status")
                .entitySchema("schema")
                .processDefinitionKey("process-id")
                .build();
        Mockito.when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito
                .when(runtimeService.startProcessInstanceByKey(definition.getProcessDefinitionKey()))
                .thenReturn(processInstance);
        Mockito.when(schemaService.getSchemaByName(definition.getEntitySchema())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(MissingSchemaException.class, () -> factory.createTransaction(definition));
    }
}
