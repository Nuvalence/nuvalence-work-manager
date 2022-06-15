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
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TransactionFactoryTest {
    @Mock
    private ProcessEngine processEngine;

    @Mock
    private EntityService entityService;

    @Mock
    private SchemaService schemaService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private ProcessInstance processInstance;

    @Mock
    private UserManagementClient userManagementClient;

    private TransactionFactory factory;
    private Clock clock;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        factory = new TransactionFactory(processEngine, entityService, schemaService, userManagementClient);
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
                .status(definition.getDefaultStatus())
                .priority("medium")
                .createdBy(new UUID(0L, 0L).toString())
                .subjectUserId(new UUID(0L, 0L).toString())
                .assignedTo("")
                .createdTimestamp(OffsetDateTime.now(clock))
                .lastUpdatedTimestamp(OffsetDateTime.now(clock))
                .build();
        transaction.loadEntity(entityService);

        // Act and Assert
        assertEquals(transaction, factory.createTransaction(definition));
    }

    @Test
    void createTransactionWithToken() throws MissingSchemaException, MissingEntityException {
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
        final WorkerToken jwt = new WorkerToken(List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "zip-uid", "Jake@statefarm.com", "token");
        final Optional<User> mockUser = createUser();
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito
                .when(runtimeService.startProcessInstanceByKey(definition.getProcessDefinitionKey()))
                .thenReturn(processInstance);
        Mockito.when(processInstance.getId()).thenReturn("process-instance-id");
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(entityService.saveEntity(new Entity(schema))).thenReturn(new Entity(schema, entityId));
        Mockito.when(entityService.getEntityById(entityId)).thenReturn(Optional.of(new Entity(schema, entityId)));
        Mockito.when(securityContext.getAuthentication()).thenReturn(jwt);
        Mockito.when(userManagementClient.getUserByEmail(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(mockUser);
        final Transaction transaction = Transaction.builder()
                .transactionDefinitionId(definition.getId())
                .transactionDefinitionKey(definition.getKey())
                .processInstanceId("process-instance-id")
                .entityId(entityId)
                .status("status")
                .priority("medium")
                .createdBy(mockUser.get().getId().toString())
                .subjectUserId(mockUser.get().getId().toString())
                .assignedTo("")
                .createdTimestamp(OffsetDateTime.now(clock))
                .lastUpdatedTimestamp(OffsetDateTime.now(clock))
                .build();
        transaction.loadEntity(entityService);

        // Act and Assert
        assertEquals(transaction, factory.createTransaction(definition));
    }

    private Optional<User> createUser() {
        return Optional.ofNullable(User.builder()
                .email("someEmail@something.com")
                .id(UUID.randomUUID())
                .build());
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
        final Optional<User> mockUser = createUser();
        Mockito.lenient().when(processEngine.getRuntimeService()).thenReturn(runtimeService);
        Mockito.lenient()
                .when(runtimeService.startProcessInstanceByKey(definition.getProcessDefinitionKey()))
                .thenReturn(processInstance);
        Mockito.lenient()
                .when(schemaService.getSchemaByName(definition.getEntitySchema())).thenReturn(Optional.empty());
        Mockito.lenient()
                .when(userManagementClient.getUserByEmail(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(mockUser);

        // Act and Assert
        assertThrows(MissingSchemaException.class, () -> factory.createTransaction(definition));
    }
}
