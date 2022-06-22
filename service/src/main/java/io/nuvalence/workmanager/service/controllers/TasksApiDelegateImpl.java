package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.generated.controllers.TasksApiDelegate;
import io.nuvalence.workmanager.service.generated.models.TaskModel;
import io.nuvalence.workmanager.service.mapper.TaskMapper;
import io.nuvalence.workmanager.service.service.WorkflowTasksService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller layer for Task management.
 */
@RequiredArgsConstructor
@Service
public class TasksApiDelegateImpl implements TasksApiDelegate {
    private final WorkflowTasksService tasksService;
    private final TaskMapper taskMapper;

    @Override
    public ResponseEntity<List<TaskModel>> getActiveTasks(UUID id) {
        final List<TaskModel> results = tasksService.getActiveTasksForTransactionByProcessId(id);

        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<List<TaskModel>> getCompletedTasks(UUID id) {
        final List<TaskModel> results = tasksService.getCompletedTasksForTransactionByProcessId(id).stream()
                .map(taskMapper.INSTANCE::historicTaskToTaskModel)
                .collect(Collectors.toList());

        return ResponseEntity.status(200).body(results);
    }
}
