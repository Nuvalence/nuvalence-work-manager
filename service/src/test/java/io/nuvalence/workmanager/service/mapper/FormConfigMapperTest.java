package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FormConfigMapperTest {
    private FormConfigDefinition formConfig;
    private FormConfigDefinitionModel formConfigModel;

    @BeforeEach
    void setup() {
        final UUID id = UUID.randomUUID();
        formConfig = FormConfigDefinition.builder()
                .id(id)
                .name("test-form-config")
                .schema("test-schema")
                .formConfigJson("test-json")
                .build();
        formConfigModel = new FormConfigDefinitionModel()
                .id(id)
                .name("test-form-config")
                .schema("test-schema")
                .formConfigJson("test-json");
    }

    @Test
    void formConfigToFormConfigModelTest() {
        assertEquals(formConfigModel, FormConfigMapper.INSTANCE.formConfigToFormConfigModel(formConfig));
        assertNull(FormConfigMapper.INSTANCE.formConfigToFormConfigModel(null));
    }

    @Test
    void formConfigModelToFormConfigTest() {
        assertEquals(formConfig, FormConfigMapper.INSTANCE.formConfigModelToFormConfig(formConfigModel));
        assertNull(FormConfigMapper.INSTANCE.formConfigModelToFormConfig(null));
    }
}
