package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.generated.models.TaskModel;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps tasks between the following 2 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.TaskModel})</li>
 *     <li>Logic Models
 *     ({@link org.camunda.bpm.engine.task.Task}),
 *     ({@link org.camunda.bpm.engine.history.HistoricTaskInstance})</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    /**
     * Maps {@link org.camunda.bpm.engine.task.Task} to
     * {@link io.nuvalence.workmanager.service.generated.models.TaskModel}.
     * @param task Logic model for Camunda task
     * @return API model for a task
     */
    default TaskModel taskToTaskModel(Task task) {
        final TaskModel model = new TaskModel();

        model.setTaskName(task.getName());
        model.setTaskDefinitionId(task.getTaskDefinitionKey());

        return model;
    }

    /**
     * Maps {@link org.camunda.bpm.engine.history.HistoricTaskInstance} to
     * {@link io.nuvalence.workmanager.service.generated.models.TaskModel}.
     * @param task Logic model for Camunda historic task instance
     * @return API model for a task
     */
    default TaskModel historicTaskToTaskModel(HistoricTaskInstance task) {
        final TaskModel model = new TaskModel();

        model.setTaskName(task.getName());
        model.setTaskDefinitionId(task.getTaskDefinitionKey());

        return model;
    }
}
