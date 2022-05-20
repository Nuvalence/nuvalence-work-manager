package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.transaction.NamedFormMapping;
import io.nuvalence.workmanager.service.domain.transaction.TaskFormMapping;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.generated.models.NamedFormMappingModel;
import io.nuvalence.workmanager.service.generated.models.TaskFormMappingModel;
import io.nuvalence.workmanager.service.generated.models.TransactionDefinitionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionDefinitionMapperTest {
    private TransactionDefinition transactionDefinition;
    private TransactionDefinitionModel model;
    private TransactionDefinitionMapper mapper;

    @BeforeEach
    void setup() {
        final UUID id = UUID.randomUUID();
        final UUID mappingId1 = UUID.randomUUID();
        final UUID mappingId2 = UUID.randomUUID();
        final UUID formId1 = UUID.randomUUID();
        final UUID formId2 = UUID.randomUUID();
        final UUID namedMappingId1 = UUID.randomUUID();
        final UUID namedMappingId2 = UUID.randomUUID();
        transactionDefinition = TransactionDefinition.builder()
                .id(id)
                .key("test")
                .name("test transaction")
                .processDefinitionKey("process-definition-key")
                .entitySchema("testschema")
                .taskFormMappings(List.of(
                        TaskFormMapping.builder()
                                .id(mappingId1)
                                .formId(formId1)
                                .taskDefinitionId("task")
                                .build(),
                        TaskFormMapping.builder()
                                .id(mappingId2)
                                .formId(formId2)
                                .taskDefinitionId("other-task")
                                .role("role")
                                .build()
                ))
                .namedFormMappings(List.of(
                        NamedFormMapping.builder()
                                .id(namedMappingId1)
                                .formConfigName("form1")
                                .formId(formId1)
                                .build(),
                        NamedFormMapping.builder()
                                .id(namedMappingId2)
                                .formConfigName("form2")
                                .formId(formId2)
                                .role("role")
                                .build()
                ))
                .build();
        model = new TransactionDefinitionModel()
                .id(id)
                .key("test")
                .name("test transaction")
                .processDefinitionKey("process-definition-key")
                .entitySchema("testschema")
                .addTaskFormMappingsItem(
                        new TaskFormMappingModel()
                                .id(mappingId1)
                                .formId(formId1)
                                .taskDefinitionId("task")
                )
                .addTaskFormMappingsItem(
                        new TaskFormMappingModel()
                                .id(mappingId2)
                                .formId(formId2)
                                .taskDefinitionId("other-task")
                                .role("role")
                )
                .addNamedFormMappingsItem(
                        new NamedFormMappingModel()
                                .id(namedMappingId1)
                                .formConfigName("form1")
                                .formId(formId1)
                )
                .addNamedFormMappingsItem(
                        new NamedFormMappingModel()
                                .id(namedMappingId2)
                                .formConfigName("form2")
                                .formId(formId2)
                                .role("role")
                );
        mapper = TransactionDefinitionMapper.INSTANCE;
    }

    @Test
    void transactionDefinitionToTransactionDefinitionModelTest() {
        assertEquals(model, mapper.transactionDefinitionToTransactionDefinitionModel(transactionDefinition));
        assertNull(mapper.transactionDefinitionToTransactionDefinitionModel(null));
    }

    @Test
    void transactionDefinitionModelToTransactionDefinitionTest() {
        assertTransactionDefinitionsEqual(
                transactionDefinition,
                mapper.transactionDefinitionModelToTransactionDefinition(model)
        );
        assertNull(mapper.transactionDefinitionModelToTransactionDefinition(null));
    }

    private void assertTransactionDefinitionsEqual(final TransactionDefinition a, final TransactionDefinition b) {
        assertEquals(a.getId(), b.getId());
        assertEquals(a.getName(), b.getName());
        assertEquals(a.getProcessDefinitionKey(), b.getProcessDefinitionKey());
        assertEquals(a.getEntitySchema(), b.getEntitySchema());
        assertEquals(a.getTaskFormMappings().size(), b.getTaskFormMappings().size());
        for (int i = 0; i < a.getTaskFormMappings().size(); i++) {
            assertEquals(a.getTaskFormMappings().get(i).getId(), b.getTaskFormMappings().get(i).getId());
            assertEquals(a.getTaskFormMappings().get(i).getFormId(), b.getTaskFormMappings().get(i).getFormId());
            assertEquals(
                    a.getTaskFormMappings().get(i).getTaskDefinitionId(),
                    b.getTaskFormMappings().get(i).getTaskDefinitionId()
            );
            assertEquals(a.getTaskFormMappings().get(i).getRole(), b.getTaskFormMappings().get(i).getRole());
        }
        assertEquals(a.getNamedFormMappings().size(), b.getNamedFormMappings().size());
        for (int j = 0; j < a.getNamedFormMappings().size(); j++) {
            assertEquals(a.getNamedFormMappings().get(j).getId(), b.getNamedFormMappings().get(j).getId());
            assertEquals(
                    a.getNamedFormMappings().get(j).getFormConfigName(),
                    b.getNamedFormMappings().get(j).getFormConfigName()
            );
            assertEquals(a.getNamedFormMappings().get(j).getFormId(), b.getNamedFormMappings().get(j).getFormId());
            assertEquals(a.getNamedFormMappings().get(j).getRole(), b.getNamedFormMappings().get(j).getRole());
        }
    }

}