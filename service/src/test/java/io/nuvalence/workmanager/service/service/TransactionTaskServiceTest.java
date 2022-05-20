package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionTaskServiceTest {

    @Mock
    private ProcessEngine processEngine;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskQuery taskQuery;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private EntityService entityService;

    private TransactionTaskService service;

    @BeforeEach
    void setup() {
        service = new TransactionTaskService(processEngine, entityMapper);
        Mockito.lenient().when(processEngine.getTaskService()).thenReturn(taskService);
        Mockito.lenient().when(taskService.createTaskQuery()).thenReturn(taskQuery);
    }

    @Test
    void completeTask() throws MissingEntityException, MissingTaskException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .processInstanceId("process-instance")
                .status("incomplete")
                .build();
        final Entity entity = new Entity(Schema.builder().build(), transaction.getEntityId());
        Mockito.when(entityService.getEntityById(entity.getId())).thenReturn(Optional.of(entity));
        transaction.loadEntity(entityService);
        Mockito.when(entityMapper.convertAttributesToGenericMap(entity)).thenReturn(Map.of("foo", "bar"));
        final Task task = new TaskEntity("task-id");
        Mockito.when(taskQuery.processInstanceId(transaction.getProcessInstanceId())).thenReturn(taskQuery);
        Mockito.when(taskQuery.taskDefinitionKey(task.getId())).thenReturn(taskQuery);
        Mockito.when(taskQuery.list()).thenReturn(List.of(task));

        // Act
        service.completeTask(transaction, task.getId());

        // Assert
        Mockito.verify(taskService).complete(task.getId(), Map.of("data", Map.of("foo", "bar")));
    }

    @Test
    void completeTaskThrowsMissingTaskExceptionWhenTaskDoesntExist() throws MissingEntityException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .processInstanceId("process-instance")
                .build();
        final Entity entity = new Entity(Schema.builder().build(), transaction.getEntityId());
        Mockito.when(entityService.getEntityById(entity.getId())).thenReturn(Optional.of(entity));
        transaction.loadEntity(entityService);
        Mockito.when(taskQuery.processInstanceId(transaction.getProcessInstanceId())).thenReturn(taskQuery);
        Mockito.when(taskQuery.taskDefinitionKey("task-id")).thenReturn(taskQuery);
        Mockito.when(taskQuery.list()).thenReturn(List.of());

        // Act and Assert
        assertThrows(MissingTaskException.class, () -> service.completeTask(transaction, "task-id"));
    }

}