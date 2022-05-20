package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.service.WorkflowTasksService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class ActionApiDelegateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowTasksService taskService;

    @Test
    void getComplainantActions() throws Exception {
        final UUID processInstanceId = UUID.randomUUID();
        final List<String> actions =
                Arrays.asList("edit", "delete", "submit");
        Mockito
                .when(taskService.getActionsForActiveTask(processInstanceId))
                        .thenReturn(actions);

        mockMvc.perform(get("/actions/{processInstanceId}", processInstanceId))
                .andExpect(jsonPath("$[0]").value(actions.get(0)))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(status().isOk());
    }

    @Test
    void getAgentActions() throws Exception {
        final UUID processInstanceId = UUID.randomUUID();
        final List<String> actions =
                Arrays.asList("edit", "approve", "request more info");
        Mockito
                .when(taskService.getActionsForActiveTask(processInstanceId))
                .thenReturn(actions);

        mockMvc.perform(get("/actions/{processInstanceId}", processInstanceId))
                .andExpect(jsonPath("$[0]").value(actions.get(0)))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(status().isOk());
    }

    @Test
    void getAction404() throws Exception {
        // if processInstanceId is blank, will throw 404 - not found
        mockMvc.perform(get("/actions/{processInstanceId}", ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAction400() throws Exception {
        // if given id is not a valid UUID
        // will throw 400 - bad request
        int invalidId = 1234;
        mockMvc.perform(get("/actions/{processInstanceId}", invalidId))
                .andExpect(status().is4xxClientError());
    }
}
