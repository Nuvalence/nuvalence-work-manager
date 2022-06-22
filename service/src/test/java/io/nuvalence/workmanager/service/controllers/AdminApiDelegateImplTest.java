package io.nuvalence.workmanager.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import io.nuvalence.workmanager.service.generated.models.FormConfigQueryParameters;
import io.nuvalence.workmanager.service.mapper.FormConfigMapper;
import io.nuvalence.workmanager.service.mapper.SchemaMapper;
import io.nuvalence.workmanager.service.mapper.TransactionDefinitionMapper;
import io.nuvalence.workmanager.service.service.FormConfigService;
import io.nuvalence.workmanager.service.service.SchemaService;
import io.nuvalence.workmanager.service.service.TransactionDefinitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
class AdminApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchemaService schemaService;

    @MockBean
    private TransactionDefinitionService transactionDefinitionService;

    @Autowired
    @MockBean
    private FormConfigService formConfigService;

    @Test
    void getSchema() throws Exception {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        Mockito.when(schemaService.getSchemaByName("testschema")).thenReturn(Optional.of(schema));

        // Act and Assert
        mockMvc.perform(get("/admin/entity/schema/testschema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testschema"));
    }

    @Test
    void getSchema404() throws Exception {
        // Arrange
        Mockito.when(schemaService.getSchemaByName("testschema")).thenReturn(Optional.empty());

        // Act and Assert
        mockMvc.perform(get("/admin/entity/schema/testschema"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSchemas() throws Exception {
        // Arrange
        final Schema schema1 = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Schema schema2 = Schema.builder()
                .name("mytest")
                .property("attribute", String.class)
                .build();
        Mockito.when(schemaService.getSchemasByPartialNameMatch("test")).thenReturn(List.of(schema1, schema2));

        // Act and Assert
        mockMvc.perform(get("/admin/entity/schema?search=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("testschema"))
                .andExpect(jsonPath("$[1].name").value("mytest"));
    }

    @Test
    void postSchema() throws Exception {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final String postBody = new ObjectMapper()
                .writeValueAsString(SchemaMapper.INSTANCE.schemaToSchemaModel(schema));

        // Act and Assert
        mockMvc.perform(
                    post("/admin/entity/schema")
                            .content(postBody)
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void getTransactionDefinition() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("test")
                .name("test transaction")
                .processDefinitionKey("process-definition-key")
                .entitySchema("test-schema")
                .defaultStatus("new")
                .build();
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionById(transactionDefinition.getId()))
                .thenReturn(Optional.of(transactionDefinition));

        // Act and Assert
        mockMvc.perform(get("/admin/transaction/" + transactionDefinition.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test transaction"));
    }

    @Test
    void getTransactionDefinition404() throws Exception {
        // Arrange
        final UUID id = UUID.randomUUID();
        Mockito.when(transactionDefinitionService.getTransactionDefinitionById(id)).thenReturn(Optional.empty());

        // Act and Assert
        mockMvc.perform(get("/admin/transaction/" + id.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionDefinitionsByCategory() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition1 = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("test-1")
                .name("test 1")
                .category("cat")
                .processDefinitionKey("process-definition-key")
                .entitySchema("test-schema")
                .defaultStatus("new")
                .build();
        final TransactionDefinition transactionDefinition2 = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("test-2")
                .name("test 2")
                .category("dog")
                .processDefinitionKey("process-definition-key")
                .entitySchema("test-schema")
                .defaultStatus("new")
                .build();
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionsByPartialCategoryMatch("dog"))
                .thenReturn(List.of(transactionDefinition1, transactionDefinition2));

        // Act and Assert
        mockMvc.perform(get("/admin/transaction/category?category=dog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category").value("cat"))
                .andExpect(jsonPath("$[1].category").value("dog"));
    }

    @Test
    void getTransactionDefinitions() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition1 = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("test-1")
                .name("test 1")
                .category("cat")
                .processDefinitionKey("process-definition-key")
                .entitySchema("test-schema")
                .defaultStatus("new")
                .build();
        final TransactionDefinition transactionDefinition2 = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("test-2")
                .name("test 2")
                .category("cat")
                .processDefinitionKey("process-definition-key")
                .entitySchema("test-schema")
                .defaultStatus("new")
                .build();
        Mockito
                .when(transactionDefinitionService.getTransactionDefinitionsByPartialNameMatch("test"))
                .thenReturn(List.of(transactionDefinition1, transactionDefinition2));

        // Act and Assert
        mockMvc.perform(get("/admin/transaction?name=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("test 1"))
                .andExpect(jsonPath("$[1].name").value("test 2"));
    }

    @Test
    void postTransactionDefinition() throws Exception {
        // Arrange
        final TransactionDefinition transactionDefinition = TransactionDefinition.builder()
                .id(UUID.randomUUID())
                .key("test")
                .name("test transaction")
                .category("test transaction")
                .processDefinitionKey("process-definition-key")
                .entitySchema("test-schema")
                .defaultStatus("new")
                .build();
        Mockito
                .when(transactionDefinitionService.saveTransactionDefinition(transactionDefinition))
                .thenReturn(transactionDefinition);
        final String postBody = new ObjectMapper().writeValueAsString(
                TransactionDefinitionMapper.INSTANCE
                        .transactionDefinitionToTransactionDefinitionModel(transactionDefinition)
        );

        // Act and Assert
        mockMvc.perform(
                        post("/admin/transaction")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test transaction"));
    }

    @Test
    void getFormConfig() throws Exception {
        Map<String, Object> test1 = new LinkedHashMap<String, Object>();
        test1.put("display", "wizard");
        test1.put("title", "test");
        test1.put("type", "form");
        // Arrange
        final FormConfigDefinition formConfigDefinition = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .schema("test-schema")
                .formConfigJson(test1)
                .build();
        Mockito
                .when(formConfigService.getFormConfigDefinitionById(formConfigDefinition.getId()))
                .thenReturn(Optional.of(formConfigDefinition));

        // Act and Assert
        mockMvc.perform(get("/admin/formconfigID/" + formConfigDefinition.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-form-config"));
    }

    @Test
    void getFormConfig404() throws Exception {
        //Arrange
        final UUID id = UUID.randomUUID();
        Mockito.when(formConfigService.getFormConfigDefinitionById(id)).thenReturn(Optional.empty());

        //Act and Assert
        mockMvc.perform(get("/admin/formconfig/" + id.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFormConfigsObject() throws Exception {
        Map<String, Object> test1 = new LinkedHashMap<String, Object>();
        test1.put("display", "wizard");
        test1.put("title", "test");
        test1.put("type", "form");

        Map<String, Object> rendererOptions = new LinkedHashMap<String, Object>();
        rendererOptions.put("language", "en");
        //Arrange
        final FormConfigDefinition formConfigDefinition1 = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .schema("test-schema")
                .formConfigJson(test1)
                .rendererOptions(rendererOptions)
                .build();

        Mockito
                .when(formConfigService.getFormConfigDefinitionsByPartialNameMatch("test"))
                .thenReturn(List.of(formConfigDefinition1));

        //Act and Assert
        mockMvc.perform(get("/admin/formconfig?search=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].formConfigJson.title").value("test"))
                .andExpect(jsonPath("$[0].rendererOptions.language").value("en"));
    }

    @Test
    void getFormConfigs() throws Exception {
        Map<String, Object> test1 = new LinkedHashMap<String, Object>();
        test1.put("display", "wizard");
        test1.put("title", "test");
        test1.put("type", "form");

        Map<String, Object> test2 = new LinkedHashMap<String, Object>();
        test1.put("display", "wizard");
        test1.put("title", "test2");
        test1.put("type", "form");
        //Arrange
        final FormConfigDefinition formConfigDefinition1 = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .schema("test-schema")
                .formConfigJson(test1)
                .build();
        final FormConfigDefinition formConfigDefinition2 = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-2")
                .schema("test-schema")
                .formConfigJson(test2)
                .build();
        Mockito
                .when(formConfigService.getFormConfigDefinitionsByPartialNameMatch("test"))
                .thenReturn(List.of(formConfigDefinition1, formConfigDefinition2));

        //Act and Assert
        mockMvc.perform(get("/admin/formconfig?search=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("test-form-config-1"))
                .andExpect(jsonPath("$[1].name").value("test-form-config-2"));
    }

    @Test
    void postFormConfig() throws Exception {
        Map<String, Object> test1 = new LinkedHashMap<String, Object>();
        test1.put("language", "en");

        Map<String, Object> formConfigJson = new LinkedHashMap<String, Object>();
        formConfigJson.put("display", "wizard");
        formConfigJson.put("title", "test");
        formConfigJson.put("type", "form");
        // Arrange
        final FormConfigDefinition formConfigDefinition = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .schema("test-schema")
                .formConfigJson(formConfigJson)
                .rendererOptions(test1)
                .schema("test")
                .description("Example description")
                .status("draft")
                .version("1")
                .createdBy("Dummy user")
                .lastUpdatedBy("Dummy user")
                .translationRequired(true)
                .build();
        Mockito
                .when(formConfigService.saveFormConfigDefinition(formConfigDefinition))
                .thenReturn(formConfigDefinition);
        final String postBody = new ObjectMapper().writeValueAsString(
                FormConfigMapper.INSTANCE
                        .formConfigToFormConfigModel(formConfigDefinition)
        );

        // Act and Assert
        mockMvc.perform(
                        post("/admin/formconfig")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-form-config"));
    }

    @Test
    void putPublishFormConfig() throws Exception {
        Map<String, Object> testJson = new LinkedHashMap<String, Object>();
        testJson.put("display", "wizard");
        testJson.put("title", "test");
        testJson.put("type", "form");

        final FormConfigDefinition formConfigDefinition = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .schema("test-schema")
                .status("published")
                .formConfigJson(testJson)
                .build();
        Mockito
                .when(formConfigService.publishFormConfig(formConfigDefinition.getId()))
                .thenReturn(Optional.of(formConfigDefinition));

        FormConfigQueryParameters body = new FormConfigQueryParameters().id(formConfigDefinition.getId());
        final String requestBody = new ObjectMapper().writeValueAsString(body);

        mockMvc.perform(
                put("/admin/formconfig/publish")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(FormConfigDefinitionModel.StatusEnum.PUBLISHED.getValue()));
    }

    @Test
    void putPublishFormConfig_invalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();

        Mockito
                .when(formConfigService.publishFormConfig(invalidId))
                .thenReturn(Optional.empty());

        FormConfigQueryParameters body = new FormConfigQueryParameters().id(invalidId);
        final String requestBody = new ObjectMapper().writeValueAsString(body);

        mockMvc.perform(
                        put("/admin/formconfig/publish")
                                .content(requestBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void putUnpublishFormConfig() throws Exception {
        Map<String, Object> testJson = new LinkedHashMap<String, Object>();
        testJson.put("display", "wizard");
        testJson.put("title", "test");
        testJson.put("type", "form");

        final FormConfigDefinition formConfigDefinition = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .schema("test-schema")
                .status("draft")
                .formConfigJson(testJson)
                .build();
        Mockito
                .when(formConfigService.unpublishFormConfig(formConfigDefinition.getId()))
                .thenReturn(Optional.of(formConfigDefinition));

        FormConfigQueryParameters body = new FormConfigQueryParameters().id(formConfigDefinition.getId());
        final String requestBody = new ObjectMapper().writeValueAsString(body);

        mockMvc.perform(
                        put("/admin/formconfig/unpublish")
                                .content(requestBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(FormConfigDefinitionModel.StatusEnum.DRAFT.getValue()));
    }

    @Test
    void putUnpublishFormConfig_invalidId() throws Exception {
        UUID invalidId = UUID.randomUUID();

        Mockito
                .when(formConfigService.publishFormConfig(invalidId))
                .thenReturn(Optional.empty());

        FormConfigQueryParameters body = new FormConfigQueryParameters().id(invalidId);
        final String requestBody = new ObjectMapper().writeValueAsString(body);

        mockMvc.perform(
                        put("/admin/formconfig/unpublish")
                                .content(requestBody)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

}
