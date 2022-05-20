package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.generated.controllers.ActionsApiDelegate;
import io.nuvalence.workmanager.service.service.WorkflowTasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Controller layer for Actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionApiDelegateImpl implements ActionsApiDelegate {
    private final WorkflowTasksService tasksService;

    @Override
    public ResponseEntity<List<String>> getActions(UUID id) {
        final List<String> results;
        try {
            results = tasksService.getActionsForActiveTask(id);
        } catch (BpmnModelException e) {
            log.error("An error occurred while finding actions for the given related task", e);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY.value()).build();
        }

        if (!results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK.value()).body(results);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build();
        }
    }
}
