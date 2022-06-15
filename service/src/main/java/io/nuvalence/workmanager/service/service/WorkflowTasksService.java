package io.nuvalence.workmanager.service.service;

import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.TaskModel;
import io.nuvalence.workmanager.service.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngine;

import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
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
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class WorkflowTasksService {
    private final ProcessEngine processEngine;
    private final TransactionDefinitionService transactionDefinitionService;

    /**
     * Gets all active tasks for a transaction.
     *
     * @param id transaction definition id to fetch tasks for
     * @return List of active tasks
    **/
    public List<TaskModel> getActiveTasksForTransactionByProcessId(final UUID id) {
        List<TaskModel> results = new ArrayList<>();
        List<Task> activeTasks = processEngine
            .getTaskService()
            .createTaskQuery()
            .active()
            .processInstanceId(id.toString())
            .list();

        for (Task task : activeTasks) {
            TaskModel taskModel = TaskMapper.INSTANCE.taskToTaskModel(task);
            String processDefinitionKey = processEngine
                    .getRepositoryService()
                    .getProcessDefinition(task.getProcessDefinitionId()).getResourceName();
            BpmnModelInstance modelInstance;
            try {
                modelInstance = Bpmn.readModelFromStream(
                        new FileInputStream(processDefinitionKey));
                UserTask userTask = modelInstance
                        .getModelElementById(task.getTaskDefinitionKey());
                Collection<SequenceFlow> outgoingSequenceFlows = userTask.getOutgoing();
                outgoingSequenceFlows.forEach(f -> {
                    FlowNode nextFlowNode = f.getTarget();
                    if (nextFlowNode instanceof ExclusiveGateway) {
                        Collection<SequenceFlow> flows = f.getTarget().getOutgoing();
                        for (SequenceFlow flow : flows) {
                            taskModel.getTaskConditions().add(flow.getName());
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                log.warn("unable to retrieve sequence flow conditions");
            }
            results.add(taskModel);
        }
        return results;
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
     * A request for public statuses maps to Camunda property publicStatus.
     * A request for internal statuses maps to Camunda property status.
     *
     **/
    public enum StatusType {
        PUBLIC("publicStatus"),
        INTERNAL("status");

        public final String propertyName;

        private StatusType(String propertyName) {
            this.propertyName = propertyName;
        }
    }

    /**
     * Gets all statuses from workflow process definitions.
     * Defined as Camunda extension properties.
     *
     * @param type public or internal, defaults to public
     * @param category optional param to search by definition category
     * @param key optional param to search by definition key
     * @return List of available statuses
     **/
    public List<String> getCamundaStatuses(final String type, String category, String key) {
        // default to public
        StatusType statusType = StatusType.PUBLIC;
        try {
            statusType = StatusType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.warn("Provided status search type {} is not enum value, defaulting to public.", type);
        }
        List<ProcessDefinition> processDefinitions = createProcessDefinitionSearchAndRetrieve(category, key);

        // use Set remove duplicates, i.e. maybe 2 statuses map to 1 public status
        LinkedHashSet<String> distinctStatuses = new LinkedHashSet<>();
        for (ProcessDefinition definition : processDefinitions) {
            try {
                BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
                        new FileInputStream(definition.getResourceName()));
                Collection<CamundaProperty> properties = modelInstance
                        .getModelElementsByType(CamundaProperty.class);
                for (CamundaProperty property : properties) {
                    if (property.getAttributeValue("name").equals(statusType.propertyName)) {
                        distinctStatuses.add(property.getCamundaValue());
                    }
                }
            } catch (FileNotFoundException e) {
                log.warn("error parsing bpmn file {} for workflow statuses", definition.getResourceName());
            }
        }
        return new ArrayList<>(distinctStatuses);
    }

    private List<ProcessDefinition> createProcessDefinitionSearchAndRetrieve(String category, String key) {
        if (category == null && key == null) {
            // get all
            return processEngine
                    .getRepositoryService()
                    .createProcessDefinitionQuery()
                    .latestVersion()
                    .list();
        } else if (category != null) {
            // a transaction definition category search could return multiple definition keys, so it has to iterate
            List<ProcessDefinition> results = new ArrayList<>();
            List<TransactionDefinition> transactionDefinitions = transactionDefinitionService
                    .getTransactionDefinitionsByPartialCategoryMatch(category);
            for (TransactionDefinition transactionDefinition : transactionDefinitions) {
                List<ProcessDefinition> resultsForKey = processEngine
                        .getRepositoryService()
                        .createProcessDefinitionQuery()
                        .processDefinitionKey(transactionDefinition.getProcessDefinitionKey())
                        .latestVersion()
                        .list();
                results.addAll(resultsForKey);
            }
            return results;
        } else {
            // else just a search by specific key provided in params
            return processEngine
                    .getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(key)
                    .latestVersion()
                    .list();
        }
    }
}
