package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.repository.TransactionDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransactionDefinitionServiceTest {
    @Mock
    private TransactionDefinitionRepository repository;
    private TransactionDefinitionService service;

    @BeforeEach
    void setup() {
        service = new TransactionDefinitionService(repository);
    }

    @Test
    void getTransactionDefinitionByIdTransactionDefinitionWhenFound() {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-transaction")
                .build();
        Mockito
                .when(repository.findById(transactionDefinition.getId()))
                .thenReturn(Optional.of(transactionDefinition));

        // Act and Assert
        assertEquals(
                Optional.of(transactionDefinition),
                service.getTransactionDefinitionById(transactionDefinition.getId())
        );
    }

    @Test
    void getTransactionDefinitionByIdEmptyOptionalWhenNotFound() {
        // Arrange
        final UUID id = UUID.randomUUID();
        Mockito
                .when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertEquals(Optional.empty(), service.getTransactionDefinitionById(id));
    }

    @Test
    void getTransactionDefinitionByKeyTransactionDefinitionWhenFound() {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("key")
                .name("test-transaction")
                .build();
        Mockito
                .when(repository.searchByKey(transactionDefinition.getKey()))
                .thenReturn(List.of(transactionDefinition));

        // Act and Assert
        assertEquals(
                Optional.of(transactionDefinition),
                service.getTransactionDefinitionByKey(transactionDefinition.getKey())
        );
    }

    @Test
    void getTransactionDefinitionByKeyEmptyOptionalWhenNotFound() {
        // Arrange
        final String key = "key";
        Mockito
                .when(repository.searchByKey(key))
                .thenReturn(Collections.emptyList());

        // Act and Assert
        assertEquals(Optional.empty(), service.getTransactionDefinitionByKey(key));
    }

    @Test
    void getTransactionDefinitionsByPartialNameMatchReturnsFoundTransactionDefinitions() {
        // Arrange
        final TransactionDefinition transactionDefinition1 = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-transaction-1")
                .build();
        final TransactionDefinition transactionDefinition2 = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-transaction-2")
                .build();
        Mockito
                .when(repository.searchByPartialName("test"))
                .thenReturn(List.of(transactionDefinition1, transactionDefinition2));

        // Act and Assert
        assertEquals(
                List.of(transactionDefinition1, transactionDefinition2),
                service.getTransactionDefinitionsByPartialNameMatch("test")
        );
    }

    @Test
    void saveTransactionDefinitionDoesNotThrowExceptionIfSaveSuccessful() {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-transaction")
                .build();
        Mockito.lenient().when(repository.save(transactionDefinition)).thenReturn(transactionDefinition);

        // Act and Assert
        assertDoesNotThrow(() -> service.saveTransactionDefinition(transactionDefinition));
    }

}
