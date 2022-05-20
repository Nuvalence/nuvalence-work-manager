package io.nuvalence.workmanager.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;

/**
 * Service layer to manage task retrieval.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkflowTasksService {
    private final ProcessEngine processEngine;

    /**
     * Gets all active tasks for a transaction.
     *
     * @param id transaction definition id to fetch tasks for
     * @return List of active tasks
    **/
    public List<Task> getActiveTasksForTransactionByProcessId(final UUID id) {
        return processEngine
            .getTaskService()
            .createTaskQuery()
            .active()
            .processInstanceId(id.toString())
            .list();
    }

    /**
     * Gets all completed tasks for a transaction.
     *
     * @param id transaction definition id to fetch completed tasks for
     * @return List of completed tasks
     **/
    public List<HistoricTaskInstance> getCompletedTasksForTransactionByProcessId(final UUID id) {
        return processEngine
                .getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(id.toString())
                .finished()
                .list();
    }

    /**
     * Returns all available actions for the related task of the given processInstanceId.
     *
     * @param processInstanceId - a processInstanceId from a transaction to retrieve related active tasks
     * @return List of available actions
     */

    public List<String> getActionsForActiveTask(final UUID processInstanceId) {
        List<String> actions = new ArrayList<>();
        final TaskService taskService = processEngine.getTaskService();
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        final List<Task> taskList = processInstanceId != null
                ? taskService.createTaskQuery()
                    .active()
                    .processInstanceId(processInstanceId.toString())
                    .list()
                : null;

        if (taskList != null) {
            for (Task task : taskList) {
                BpmnModelInstance bpmnModelInstance = repositoryService
                        .getBpmnModelInstance(task.getProcessDefinitionId());
                Collection<UserTask> userTaskDefs = bpmnModelInstance.getModelElementsByType(UserTask.class);
                for (UserTask userTaskDef : userTaskDefs) {
                    if (userTaskDef.getId().equals(task.getTaskDefinitionKey())) {
                        if (userTaskDef.getExtensionElements()
                                .getElementsQuery()
                                .filterByType(CamundaProperties.class)
                                .list()
                                .size() == 0) {
                            throw new BpmnModelException();
                        }
                        CamundaProperties extensionsProperty = userTaskDef.getExtensionElements()
                                .getElementsQuery()
                                .filterByType(CamundaProperties.class)
                                .singleResult();
                        Collection<CamundaProperty> camundaProperties = extensionsProperty.getCamundaProperties();

                        for (CamundaProperty property : camundaProperties) {
                            actions.add(property.getCamundaValue());
                        }
                    }
                }
            }
        }

        return actions;
    }
}
