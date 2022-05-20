package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow;
import io.nuvalence.workmanager.service.mapper.SchemaMapper;
import io.nuvalence.workmanager.service.repository.SchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SchemaServiceTest {
    @Mock
    private SchemaRepository schemaRepository;
    private SchemaService schemaService;

    @BeforeEach
    void setup() {
        schemaService = new SchemaService(schemaRepository);
    }

    @Test
    void getSchemaByNameReturnsSchemaWhenFound() throws JsonProcessingException {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final SchemaRow row = SchemaMapper.INSTANCE.schemaToSchemaRow(schema);
        Mockito.when(schemaRepository.findById(schema.getName())).thenReturn(Optional.ofNullable(row));

        // Act and Assert
        assertEquals(Optional.of(schema), schemaService.getSchemaByName(schema.getName()));
    }

    @Test
    void getSchemaByNameReturnsEmptyOptionalWhenSchemaNotFound() throws JsonProcessingException {
        // Arrange
        Mockito.when(schemaRepository.findById("testschema")).thenReturn(Optional.empty());

        // Act and Assert
        assertEquals(Optional.empty(), schemaService.getSchemaByName("testschema"));
    }

    @Test
    void getSchemaByNameThrowsRuntimeExceptionWhenJsonCannotBeParsed() {
        // Arrange
        final SchemaRow row = SchemaRow.builder()
                .name("testschema")
                .schemaJson("Not JSON")
                .build();
        Mockito.when(schemaRepository.findById(row.getName())).thenReturn(Optional.of(row));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> schemaService.getSchemaByName(row.getName()));
    }

    @Test
    void getSchemasByPartialNameMatchReturnsFoundSchemas() throws JsonProcessingException {
        // Arrange
        final Schema schema1 = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Schema schema2 = Schema.builder()
                .name("mytest")
                .property("attribute", String.class)
                .build();
        final SchemaRow row1 = SchemaMapper.INSTANCE.schemaToSchemaRow(schema1);
        final SchemaRow row2 = SchemaMapper.INSTANCE.schemaToSchemaRow(schema2);
        Mockito.when(schemaRepository.searchByPartialName("test")).thenReturn(List.of(row1, row2));

        // Act and Assert
        assertEquals(List.of(schema1, schema2), schemaService.getSchemasByPartialNameMatch("test"));
    }

    @Test
    void getSchemasByPartialNameMatchThrowsRuntimeExceptionWhenJsonCannotBeParsed() throws JsonProcessingException {
        // Arrange
        final SchemaRow row = SchemaRow.builder()
                .name("testschema")
                .schemaJson("Not JSON")
                .build();
        Mockito.when(schemaRepository.searchByPartialName("test")).thenReturn(List.of(row));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> schemaService.getSchemasByPartialNameMatch("test"));
    }

    @Test
    void saveSchemaDoesNotThrowExceptionIfSaveSuccessful() throws JsonProcessingException {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final SchemaRow row = SchemaMapper.INSTANCE.schemaToSchemaRow(schema);
        Mockito.lenient().when(schemaRepository.save(row)).thenReturn(row);

        // Act and Assert
        assertDoesNotThrow(() -> schemaService.saveSchema(schema));
    }
}