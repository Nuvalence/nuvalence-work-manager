package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.models.TransactionFilters;
import io.nuvalence.workmanager.service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
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
    void createTransactionWithToken() throws MissingSchemaException, MissingEntityException {
        // Arrange
        final TransactionDefinition definition = TransactionDefinition.builder().build();
        final Transaction transaction = Transaction.builder().build();
        final String token = "token";
        Mockito
                .when(factory.createTransaction(definition))
                .thenReturn(transaction);

        // Act
        service.createTransaction(definition, token);

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
    void completeTask() throws MissingTaskException, JsonProcessingException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .build();

        // Act
        service.completeTask(transaction, "taskId", "foo");

        // Assert
        Mockito.verify(transactionTaskService).completeTask(transaction, "taskId", "foo");
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
    void getFilteredTransactions() throws MissingEntityException {
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
        final TransactionFilters filters = TransactionFilters.builder()
                .transactionDefinitionKey("dummy")
                .category("test")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now())
                .priority("medium")
                .status("low")
                .sortCol("id")
                .sortDir("asc")
                .pageNumber(0)
                .pageSize(25)
                .build();

        final Page<Transaction> pagedResults = new PageImpl<>(List.of(transaction1, transaction2));

        Mockito
                .when(repository.findAll(ArgumentMatchers.any(),
                        ArgumentMatchers.<Pageable>any()))
                .thenReturn(pagedResults);
        Mockito
                .when(entityService.getEntityById(entity1.getId()))
                .thenReturn(Optional.of(entity1));
        Mockito
                .when(entityService.getEntityById(entity2.getId()))
                .thenReturn(Optional.of(entity2));

        // Act and Assert
        assertEquals(pagedResults, service.getFilteredTransactions(filters));
    }
}
