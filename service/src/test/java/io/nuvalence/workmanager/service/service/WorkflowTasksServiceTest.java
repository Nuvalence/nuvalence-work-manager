package io.nuvalence.workmanager.service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class WorkflowTasksServiceTest {
    @Mock
    private WorkflowTasksService service;

    @Test
    void getAvailableStatuses() {
        assertNotNull(service.getCamundaStatuses("public", null, null));
    }
}
