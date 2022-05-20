package io.nuvalence.workmanager.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.TransactionCreationRequest;
import io.nuvalence.workmanager.service.generated.models.TransactionUpdateRequest;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.service.EntityService;
import io.nuvalence.workmanager.service.service.TransactionDefinitionService;
import io.nuvalence.workmanager.service.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("local")
class TransactionApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private EntityService entityService;

    @MockBean
    private TransactionDefinitionService transactionDefinitionService;

    @Test
    void getTransaction() throws Exception {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("incomplete")
                .priority("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(transaction.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        transaction.loadEntity(entityService);
        Mockito.when(transactionService.getTransactionById(transaction.getId())).thenReturn(Optional.of(transaction));

        // Act and Assert
        mockMvc.perform(get("/transaction/" + transaction.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getTransaction404() throws Exception {
        // Arrange
        final UUID transactionId = UUID.randomUUID();
        Mockito.when(transactionService.getTransactionById(transactionId)).thenReturn(Optional.empty());

        // Act and Assert
        mockMvc.perform(get("/transaction/" + transactionId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionMissingEntity() throws Exception {
        // Arrange
        final UUID transactionId = UUID.randomUUID();
        Mockito.when(transactionService.getTransactionById(transactionId)).thenThrow(MissingEntityException.class);

        // Act and Assert
        mockMvc.perform(get("/transaction/" + transactionId.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTransactionsByCategory()  throws Exception {
        // Arrange
        final Transaction userTransaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(userTransaction1.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        userTransaction1.loadEntity(entityService);

        Mockito
                .when(transactionService.getTransactionsByCategory("test"))
                .thenReturn(List.of(userTransaction1));

        // Act and Assert
        mockMvc.perform(get("/transaction/category?category=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTransactionsByUser()  throws Exception {
        // Arrange
        final Transaction userTransaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(userTransaction1.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        userTransaction1.loadEntity(entityService);

        final Transaction userTransaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test 2")
                .processInstanceId("Dummy user test 2")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(userTransaction2.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        userTransaction2.loadEntity(entityService);

        Mockito
                .when(transactionService.getTransactionsByUser("Dummy user"))
                .thenReturn(List.of(userTransaction1, userTransaction2));

        // Act and Assert
        mockMvc.perform(get("/transaction/user/all"))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactionsByUserMissingEntity()  throws Exception {
        // Arrange
        Mockito
                .when(transactionService.getTransactionsByUser("Dummy user"))
                .thenThrow(MissingEntityException.class);

        // Act and Assert
        mockMvc.perform(get("/transaction/user/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTransactions() throws Exception {
        // Arrange
        final Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(transaction1.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        transaction1.loadEntity(entityService);

        final Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test 2")
                .processInstanceId("Dummy user test 2")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(transaction2.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        transaction2.loadEntity(entityService);

        Mockito
                .when(transactionService.getTransactionsForDefinition("key"))
                .thenReturn(List.of(transaction1, transaction2));

        // Act and Assert
        mockMvc.perform(get("/transaction?transactionDefinitionKey=key"))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactionsMissingEntity() throws Exception {
        // Arrange
        Mockito
                .when(transactionService.getTransactionsForDefinition("key"))
                .thenThrow(MissingEntityException.class);

        // Act and Assert
        mockMvc.perform(get("/transaction?transactionDefinitionKey=key"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postTransaction() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("key")
                .processDefinitionKey("key")
                .build();
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionByKey("key"))
                .thenReturn(Optional.of(transactionDefinition));

        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();
        Mockito
                .when(entityService.getEntityById(transaction.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        transaction.loadEntity(entityService);
        Mockito.when(transactionService.createTransaction(transactionDefinition)).thenReturn(transaction);

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
            post("/transaction")
                    .content(postBody)
                    .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.getId().toString()));
    }

    @Test
    void postTransactionMissingTransactionDefinition() throws Exception {
        // Arrange
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionByKey("key"))
                .thenReturn(Optional.empty());

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                post("/transaction")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isFailedDependency());
    }

    @Test
    void postTransactionMissingEntity() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("key")
                .processDefinitionKey("key")
                .build();
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionByKey("key"))
                .thenReturn(Optional.of(transactionDefinition));
        Mockito
                .when(transactionService.createTransaction(transactionDefinition))
                .thenThrow(MissingEntityException.class);

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                post("/transaction")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isFailedDependency());
    }

    @Test
    void postTransactionMissingSchema() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("key")
                .processDefinitionKey("key")
                .build();
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionByKey("key"))
                .thenReturn(Optional.of(transactionDefinition));
        Mockito
                .when(transactionService.createTransaction(transactionDefinition))
                .thenThrow(MissingSchemaException.class);

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                post("/transaction")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isFailedDependency());
    }

    @Test
    void updateTransactionWithoutTaskId() throws Exception {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("incomplete")
                .priority("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(transaction.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().property("foo", String.class).build())));
        transaction.loadEntity(entityService);
        Mockito.when(transactionService.getTransactionById(transaction.getId())).thenReturn(Optional.of(transaction));
        Mockito.when(transactionService.updateTransaction(transaction)).thenReturn(transaction);

        final TransactionUpdateRequest request = new TransactionUpdateRequest().putDataItem("foo", "bar");
        request.setStatus("incomplete");
        request.setPriority("low");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                        post("/transaction/" + transaction.getId().toString())
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.getId().toString()))
                .andExpect(jsonPath("$.data.foo").value("bar"));
    }

    @Test
    void updateTransaction404() throws Exception {
        // Arrange
        final UUID transactionId = UUID.randomUUID();
        Mockito.when(transactionService.getTransactionById(transactionId)).thenReturn(Optional.empty());
        final TransactionUpdateRequest request = new TransactionUpdateRequest().putDataItem("foo", "bar");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                        post("/transaction/" + transactionId.toString())
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTransactionMissingEntity() throws Exception {
        // Arrange
        final UUID transactionId = UUID.randomUUID();
        Mockito.when(transactionService.getTransactionById(transactionId)).thenThrow(MissingEntityException.class);

        final TransactionUpdateRequest request = new TransactionUpdateRequest().putDataItem("foo", "bar");
        request.setStatus("incomplete");
        request.setPriority("low");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                        post("/transaction/" + transactionId.toString())
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isFailedDependency());
    }

    @Test
    void updateTransactionAndCompleteTask() throws Exception {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(transaction.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().property("foo", String.class).build())));
        transaction.loadEntity(entityService);
        Mockito.when(transactionService.getTransactionById(transaction.getId())).thenReturn(Optional.of(transaction));
        Mockito.when(transactionService.updateTransaction(transaction)).thenReturn(transaction);

        final TransactionUpdateRequest request = new TransactionUpdateRequest().putDataItem("foo", "bar");
        request.setStatus("incomplete");
        request.setPriority("low");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                        post("/transaction/" + transaction.getId().toString() + "/?taskId=taskId")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.getId().toString()))
                .andExpect(jsonPath("$.data.foo").value("bar"));
        Mockito.verify(transactionService).completeTask(transaction, "taskId");
    }

    @Test
    void updateTransactionMissingTask() throws Exception {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("incomplete")
                .priority("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(transaction.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().property("foo", String.class).build())));
        transaction.loadEntity(entityService);
        Mockito.when(transactionService.getTransactionById(transaction.getId())).thenReturn(Optional.of(transaction));
        Mockito.when(transactionService.updateTransaction(transaction)).thenReturn(transaction);
        Mockito
                .doThrow(MissingTaskException.class)
                .when(transactionService)
                .completeTask(transaction, "taskId");

        final TransactionUpdateRequest request = new TransactionUpdateRequest().putDataItem("foo", "bar");
        request.setStatus("incomplete");
        request.setPriority("low");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                        post("/transaction/" + transaction.getId().toString() + "/?taskId=taskId")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isFailedDependency());
    }
}
