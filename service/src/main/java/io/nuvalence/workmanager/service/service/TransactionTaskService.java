package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Service to handle task interactions on transactions.
 */
@Component
@RequiredArgsConstructor
public class TransactionTaskService {
    private final ProcessEngine processEngine;
    private final EntityMapper entityMapper;

    /**
     * Completes the given task, posting to the workflow the data in the transaction.
     *
     * @param transaction Transaction to complete task on
     * @param taskId ID of task to complete
     * @throws MissingTaskException If the process instance for this transaction does not have a task matching taskId
     */
    public void completeTask(final Transaction transaction, final String taskId) throws MissingTaskException {
        final TaskService taskService = processEngine.getTaskService();
        final Task task = taskService.createTaskQuery()
                .processInstanceId(transaction.getProcessInstanceId())
                .taskDefinitionKey(taskId)
                .list()
                .stream()
                .findFirst()
                .orElseThrow(() -> new MissingTaskException(transaction, taskId));

        if (!transaction.getStatus().equals("incomplete")) {
            taskService.complete(task.getId(), Map.of("status", transaction.getStatus()));
        } else {
            taskService.complete(task.getId(),
                    Map.of("data", entityMapper.convertAttributesToGenericMap(transaction.getData())));
        }
    }
}
