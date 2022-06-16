package io.nuvalence.workmanager.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.auth.WorkerToken;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.TransactionCountByStatusModel;
import io.nuvalence.workmanager.service.generated.models.TransactionCreationRequest;
import io.nuvalence.workmanager.service.generated.models.TransactionUpdateRequest;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.mapper.OffsetDateTimeMapper;
import io.nuvalence.workmanager.service.models.TransactionFilters;
import io.nuvalence.workmanager.service.service.EntityService;
import io.nuvalence.workmanager.service.service.TransactionDefinitionService;
import io.nuvalence.workmanager.service.service.TransactionService;
import io.nuvalence.workmanager.service.usermanagementapi.UserManagementClient;
import io.nuvalence.workmanager.service.usermanagementapi.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
class TransactionApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private EntityService entityService;

    @MockBean
    private TransactionDefinitionService transactionDefinitionService;

    @MockBean
    private WorkerToken workerToken;

    @MockBean
    private UserManagementClient userManagementClient;

    @BeforeEach
    void setup() {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(createWorkerToken());
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getTransaction() throws Exception {
        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("incomplete")
                .priority("low")
                .createdBy(testUser.get().getId().toString())
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
    void getTransactionMissingAuthorization() throws Exception {
        final UUID transactionId = UUID.randomUUID();
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/transaction/user/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTransactionsByCategory()  throws Exception {
        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

        // Arrange
        final Transaction userTransaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy(testUser.get().getId().toString())
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
    void getFilteredTransactions() throws Exception {
        // Arrange
        final Transaction userTransaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("dummy")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy("Dummy user")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        final TransactionFilters filters = TransactionFilters.builder()
                .transactionDefinitionKey("dummy")
                .category("test")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now())
                .priority(List.of("medium"))
                .status(List.of("new"))
                .assignedTo(List.of(UUID.randomUUID().toString()))
                .sortCol("id")
                .sortDir("asc")
                .pageNumber(0)
                .pageSize(25)
                .build();

        Mockito
                .when(entityService.getEntityById(any()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        userTransaction1.loadEntity(entityService);

        final Page<Transaction> pagedResults = new PageImpl<>(List.of(userTransaction1));

        Mockito
                .when(transactionService.getFilteredTransactions(any()))
                .thenReturn(pagedResults);

        // Act and Assert
        mockMvc.perform(get("/transaction/search?"
                        + "transactionDefinitionKey=" + filters.getTransactionDefinitionKey()
                        + "&category=" + filters.getCategory()
                        + "&startDate=" + OffsetDateTimeMapper.INSTANCE.toString(filters.getStartDate())
                        + "&endDate=" + OffsetDateTimeMapper.INSTANCE.toString(filters.getEndDate())
                        + "&priority=" + filters.getPriority().get(0)
                        + "&status=" + filters.getStatus().get(0)
                        + "&assignedTo=" + filters.getAssignedTo().get(0)
                        + "&sortCol=" + filters.getSortCol()
                        + "&sortDir=" + filters.getSortDir()
                        + "&pageNumber=" + filters.getPageNumber()
                        + "&pageSize=" + filters.getPageSize()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalPages", comparesEqualTo(1)))
                .andExpect(jsonPath("$.totalCount", comparesEqualTo(1)));
    }

    @Test
    void getTransactionsByUser()  throws Exception {
        Mockito
                .when(workerToken.getUserEmail())
                .thenReturn("someEmail@email.com");

        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

        Mockito
                .when(userManagementClient.getUserByEmail(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

        // Arrange
        final Transaction userTransaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy(testUser.get().getId().toString())
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
                .createdBy(testUser.get().getId().toString())
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();

        Mockito
                .when(entityService.getEntityById(userTransaction2.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        userTransaction2.loadEntity(entityService);

        Mockito
                .when(transactionService.getTransactionsByUser(testUser.get().getId().toString()))
                .thenReturn(List.of(userTransaction1, userTransaction2));

        // Act and Assert
        mockMvc.perform(get("/transaction/user/all"))
                .andExpect(status().isOk());

        verify(userManagementClient, times(1))
                .getUserById(anyString(), anyString());
    }

    @Test
    void getTransactionsByUserMissingEntity()  throws Exception {
        Mockito
                .when(workerToken.getUserEmail())
                .thenReturn("someEmail@email.com");

        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserByEmail(ArgumentMatchers.anyString(),
                                ArgumentMatchers.anyString()))
                .thenReturn(testUser);
                
        // Arrange
        Mockito
                .when(transactionService.getTransactionsByUser(testUser.get().getId().toString()))
                .thenThrow(MissingEntityException.class);

        // Act and Assert
        mockMvc.perform(get("/transaction/user/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTransactions() throws Exception {
        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

        // Arrange
        final Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("low")
                .createdBy(testUser.get().getId().toString())
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
                .createdBy(testUser.get().getId().toString())
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
        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

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
                .createdBy(testUser.get().getId().toString())
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .build();
        Mockito
                .when(entityService.getEntityById(transaction.getEntityId()))
                .thenReturn(Optional.of(new Entity(Schema.builder().build())));
        transaction.loadEntity(entityService);
        Mockito.when(transactionService.createTransaction(transactionDefinition, "token")).thenReturn(transaction);

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
            post("/transaction")
                    .header("Authorization", "token")
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
                .when(transactionService.createTransaction(transactionDefinition, "token"))
                .thenThrow(MissingEntityException.class);

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                post("/transaction")
                        .header("Authorization", "token")
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
                .when(transactionService.createTransaction(transactionDefinition, "token"))
                .thenThrow(MissingSchemaException.class);

        final TransactionCreationRequest request = new TransactionCreationRequest().transactionDefinitionKey("key");
        final String postBody = new ObjectMapper().writeValueAsString(request);

        // Act and Assert
        mockMvc.perform(
                post("/transaction")
                        .header("Authorization", "token")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isFailedDependency());
    }

    @Test
    void updateTransactionWithoutTaskId() throws Exception {
        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionDefinitionId(UUID.randomUUID())
                .transactionDefinitionKey("Dummy user test")
                .processInstanceId("Dummy user test")
                .entityId(UUID.randomUUID())
                .status("incomplete")
                .priority("low")
                .createdBy(testUser.get().getId().toString())
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
        request.setCondition("foo");
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
        request.setCondition("foo");
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
        Optional<User> testUser = createUser();

        Mockito
                .when(userManagementClient.getUserById(ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString()))
                .thenReturn(testUser);

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
        request.setCondition("foo");
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
        Mockito.verify(transactionService).completeTask(transaction, "taskId", "foo");
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
                .completeTask(transaction, "taskId", "foo");

        final TransactionUpdateRequest request = new TransactionUpdateRequest().putDataItem("foo", "bar");
        request.setCondition("foo");
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

    @Test
    void getTransactionCountByStatus() throws Exception {
        // Arrange

        final TransactionFilters filters = TransactionFilters.builder()
                .transactionDefinitionKey("dummy")
                .category("test")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now())
                .priority(List.of("medium"))
                .status(List.of("new"))
                .assignedTo(List.of(UUID.randomUUID().toString()))
                .build();

        final TransactionCountByStatusModel count = new TransactionCountByStatusModel();
        count.setStatus("new");
        count.setCount(123);

        Mockito.when(transactionService.getTransactionCountsByStatus(any())).thenReturn(List.of(count));

        // Act and Assert
        mockMvc.perform(get("/transaction/statuses/count?"
                + "transactionDefinitionKey=" + filters.getTransactionDefinitionKey()
                + "&category=" + filters.getCategory()
                + "&startDate=" + OffsetDateTimeMapper.INSTANCE.toString(filters.getStartDate())
                + "&endDate=" + OffsetDateTimeMapper.INSTANCE.toString(filters.getEndDate())
                + "&priority=" + filters.getPriority().get(0)
                + "&status=" + filters.getStatus().get(0)
                + "&assignedTo=" + filters.getAssignedTo().get(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", comparesEqualTo("new")))
                .andExpect(jsonPath("$[0].count", comparesEqualTo(123)));
    }

    private Optional<User> createUser() {
        return Optional.ofNullable(User.builder()
                .email("someEmail@something.com")
                .id(UUID.randomUUID())
                .build());
    }

    private WorkerToken createWorkerToken() {
        return new WorkerToken(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "someUserId",
                "someEmail@email.com",
                "originalToken"
        );
    }

}
