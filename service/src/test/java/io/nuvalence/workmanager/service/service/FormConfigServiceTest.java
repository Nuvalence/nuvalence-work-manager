package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.generated.models.FormConfigDefinitionModel;
import io.nuvalence.workmanager.service.repository.FormConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class FormConfigServiceTest {
    @Mock
    private FormConfigRepository repository;
    private FormConfigService service;

    @BeforeEach
    void setup() {
        service = new FormConfigService(repository);
    }

    @Test
    void getFormConfigByIdFormConfigWhenFound() {
        // Arrange
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .build();
        Mockito
                .when(repository.findById(formConfig.getId()))
                .thenReturn(Optional.of(formConfig));

        // Act and Assert
        assertEquals(
                Optional.of(formConfig),
                service.getFormConfigDefinitionById(formConfig.getId())
        );
    }

    @Test
    void getFormConfigByIdEmptyOptionalWhenNotFound() {
        // Arrange
        final UUID id = UUID.randomUUID();
        Mockito
                .when(repository.findById(id))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertEquals(Optional.empty(), service.getFormConfigDefinitionById(id));
    }

    @Test
    void getFormConfigsByPartialNameMatchReturnsFoundFormConfigs() {
        // Arrange
        final FormConfigDefinition formConfig1 = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .build();
        final FormConfigDefinition formConfig2 = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-2")
                .build();
        Mockito
                .when(repository.searchByPartialName("test"))
                .thenReturn(List.of(formConfig1, formConfig2));

        // Act and Assert
        assertEquals(
                List.of(formConfig1, formConfig2),
                service.getFormConfigDefinitionsByPartialNameMatch("test")
        );
    }

    @Test
    void saveFormConfigDoesNotThrowExceptionIfSaveSuccessful() throws JsonProcessingException {
        // Arrange
        Map<String, Object> rendererOptions = new LinkedHashMap<String, Object>();
        rendererOptions.put("i18n", "en");
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .rendererOptions(rendererOptions)
                .build();
        Mockito.lenient().when(repository.save(formConfig)).thenReturn(formConfig);

        // Act and Assert
        assertDoesNotThrow(() -> service.saveFormConfigDefinition(formConfig));
    }

    @Test
    void publishFormConfiguration() {
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .status("draft")
                .build();

        Mockito.when(service.getFormConfigDefinitionById(formConfig.getId())).thenReturn(Optional.of(formConfig));
        Mockito.when(repository.save(formConfig)).thenReturn(formConfig);

        Optional<FormConfigDefinition> received = service.publishFormConfig(formConfig.getId());

        assertTrue(received.isPresent());
        assertEquals(FormConfigDefinitionModel.StatusEnum.PUBLISHED.toString(), received.get().getStatus());
    }

    @Test
    void publishFormConfiguration_invalidId() {
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .status("draft")
                .build();

        Mockito.when(service.getFormConfigDefinitionById(formConfig.getId())).thenReturn(Optional.empty());

        Optional<FormConfigDefinition> received = service.publishFormConfig(formConfig.getId());

        assertTrue(received.isEmpty());
    }

    @Test
    void unpublishFormConfiguration() {
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .status("published")
                .build();

        Mockito.when(service.getFormConfigDefinitionById(formConfig.getId())).thenReturn(Optional.of(formConfig));
        Mockito.when(repository.save(formConfig)).thenReturn(formConfig);

        Optional<FormConfigDefinition> received = service.unpublishFormConfig(formConfig.getId());

        assertTrue(received.isPresent());
        assertEquals(FormConfigDefinitionModel.StatusEnum.DRAFT.getValue(), received.get().getStatus());
    }

    @Test
    void unpublishFormConfiguration_invalidId() {
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config-1")
                .status("published")
                .build();

        Mockito.when(service.getFormConfigDefinitionById(formConfig.getId())).thenReturn(Optional.empty());

        Optional<FormConfigDefinition> received = service.unpublishFormConfig(formConfig.getId());

        assertTrue(received.isEmpty());
    }

    @Test
    void saveFormConfigTranslationRequiredCheck() {
        // Arrange
        Map<String, Object> rendererOptions = new LinkedHashMap<String, Object>();

        Map<String, Object> languages  = new LinkedHashMap<String, Object>();
        languages.put("es", "{Test}");
        languages.put("en", "{Test}");
        rendererOptions.put("i18n", languages);
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .rendererOptions(rendererOptions)
                .translationRequired(false)
                .build();
        Mockito.lenient().when(repository.save(formConfig)).thenReturn(formConfig);

        assertEquals(true, service.saveFormConfigDefinition(formConfig).getTranslationRequired());
    }
}
