package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionFactory factory;

    @Mock
    private TransactionTaskService transactionTaskService;

    @Mock
    private EntityService entityService;

    private TransactionService service;

    @BeforeEach
    void setup() {
        service = new TransactionService(repository, factory, transactionTaskService, entityService);
    }

    @Test
    void createTransaction() throws MissingSchemaException, MissingEntityException {
        // Arrange
        final TransactionDefinition definition = TransactionDefinition.builder().build();
        final Transaction transaction = Transaction.builder().build();
        Mockito
                .when(factory.createTransaction(definition))
                .thenReturn(transaction);

        // Act
        service.createTransaction(definition);

        // Assert
        Mockito.verify(repository).save(transaction);
    }

    @Test
    void getTransactionByIdFound() throws MissingEntityException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .build();
        final Entity entity = new Entity(Schema.builder().build(), transaction.getEntityId());
        Mockito
                .when(repository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));
        Mockito
                .when(entityService.getEntityById(entity.getId()))
                .thenReturn(Optional.of(entity));

        // Act and Assert
        assertEquals(
                Optional.of(transaction),
                service.getTransactionById(transaction.getId())
        );
    }

    @Test
    void getTransactionByIdNotFound() throws MissingEntityException {
        // Arrange
        final UUID id = UUID.randomUUID();
        Mockito
                .when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertTrue(service.getTransactionById(id).isEmpty());
    }

    @Test
    void updateTransaction() throws MissingEntityException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .build();
        final Entity entity = new Entity(Schema.builder().build(), transaction.getEntityId());
        Mockito
                .when(repository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));
        Mockito
                .when(entityService.getEntityById(entity.getId()))
                .thenReturn(Optional.of(entity));
        transaction.loadEntity(entityService);

        // Act
        final Transaction result = service.updateTransaction(transaction);

        // Assert
        Mockito.verify(entityService).saveEntity(entity);
        assertEquals(transaction, result);
    }

    @Test
    void completeTask() throws MissingTaskException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .build();

        // Act
        service.completeTask(transaction, "taskId");

        // Assert
        Mockito.verify(transactionTaskService).completeTask(transaction, "taskId");
    }

    @Test
    void getTransactionsForDefinition() throws MissingEntityException {
        // Arrange
        final Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .build();
        final Entity entity1 = new Entity(Schema.builder().build(), transaction1.getEntityId());
        final Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .build();
        final Entity entity2 = new Entity(Schema.builder().build(), transaction2.getEntityId());
        Mockito
                .when(repository.searchByTransactionDefinitionKey("key"))
                .thenReturn(List.of(transaction1, transaction2));
        Mockito
                .when(entityService.getEntityById(entity1.getId()))
                .thenReturn(Optional.of(entity1));
        Mockito
                .when(entityService.getEntityById(entity2.getId()))
                .thenReturn(Optional.of(entity2));

        // Act and Assert
        assertEquals(List.of(transaction1, transaction2), service.getTransactionsForDefinition("key"));
    }

    @Test
    void getTransactionsByUser() throws MissingEntityException {
        // Arrange
        final Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .createdBy("user")
                .build();
        final Entity entity1 = new Entity(Schema.builder().build(), transaction1.getEntityId());
        final Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .createdBy("user")
                .build();
        final Entity entity2 = new Entity(Schema.builder().build(), transaction2.getEntityId());
        Mockito
                .when(repository.searchByTransactionByUser("user"))
                .thenReturn(List.of(transaction1, transaction2));
        Mockito
                .when(entityService.getEntityById(entity1.getId()))
                .thenReturn(Optional.of(entity1));
        Mockito
                .when(entityService.getEntityById(entity2.getId()))
                .thenReturn(Optional.of(entity2));

        // Act and Assert
        assertEquals(List.of(transaction1, transaction2), service.getTransactionsByUser("user"));
    }

    @Test
    void updateTransactionPriority() throws Exception {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .priority("low")
                .build();
        final Entity entity = new Entity(Schema.builder().build(), transaction.getEntityId());
        Mockito
                .when(repository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));
        Mockito
                .when(entityService.getEntityById(entity.getId()))
                .thenReturn(Optional.of(entity));
        transaction.loadEntity(entityService);

        // Act
        transaction.setPriority("urgent");

        // Assert
        assertEquals(service.updateTransaction(transaction).getPriority(), "urgent");
        assertEquals(transaction, service.updateTransaction(transaction));
    }

}