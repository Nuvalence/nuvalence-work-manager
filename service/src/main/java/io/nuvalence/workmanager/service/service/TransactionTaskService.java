package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.transaction.MissingTaskException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TransactionTaskService {
    private final ProcessEngine processEngine;
    private final EntityMapper entityMapper;

    /**
     * Completes the given task, posting to the workflow the data in the transaction.
     *
     * @param transaction Transaction to complete task on
     * @param taskId ID of task to complete
     * @param condition optional condition passed that influences decisions in workflow
     * @throws MissingTaskException If the process instance for this transaction does not have a task matching taskId
     * @throws JsonProcessingException If the data could not be serialized to JSON
     */
    public void completeTask(final Transaction transaction, final String taskId, final String condition)
            throws MissingTaskException, JsonProcessingException {
        final TaskService taskService = processEngine.getTaskService();
        final Task task = taskService.createTaskQuery()
                .processInstanceId(transaction.getProcessInstanceId())
                .taskDefinitionKey(taskId)
                .list()
                .stream()
                .findFirst()
                .orElseThrow(() -> new MissingTaskException(transaction, taskId));

        Map<String, Object> dataMap = entityMapper.convertAttributesToGenericMap(transaction.getData());
        taskService.complete(task.getId(), Map.of(
                "data", dataMap,
                "dataJSON", new ObjectMapper().writeValueAsString(dataMap),
                "condition", condition)
        );

        // get the status from the execution entity set by the sequence flow listener
        // for updating a Transaction look into the historyService since taskService.completeTask has happened
        try {
            String status =
                    processEngine.getHistoryService()
                            .createHistoricVariableInstanceQuery()
                            .processInstanceIdIn(task.getProcessInstanceId())
                            .variableName("status")
                            .singleResult()
                            .getValue().toString();
            transaction.setStatus(status);
        } catch (Exception e) {
            // if no status set in workflow, keep the same
            log.warn("No status set for sequence flow in Camunda workflow");
        }
    }
}
