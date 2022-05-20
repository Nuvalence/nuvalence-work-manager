package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import io.nuvalence.workmanager.service.repository.FormConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        final FormConfigDefinition formConfig = FormConfigDefinition.builder()
                .id(UUID.randomUUID())
                .name("test-form-config")
                .build();
        Mockito.lenient().when(repository.save(formConfig)).thenReturn(formConfig);

        // Act and Assert
        assertDoesNotThrow(() -> service.saveFormConfigDefinition(formConfig));
    }
}
