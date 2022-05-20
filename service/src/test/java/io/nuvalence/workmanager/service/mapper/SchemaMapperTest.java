package io.nuvalence.workmanager.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotBlankConstraint;
import io.nuvalence.workmanager.service.generated.models.AttributeDefinitionModel;
import io.nuvalence.workmanager.service.generated.models.NotBlankConstraintModel;
import io.nuvalence.workmanager.service.generated.models.SchemaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaMapperTest {
    private Schema schema;
    private SchemaModel schemaModel;
    private SchemaRow schemaRow;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Schema childSchema = Schema.builder()
                .name("common/Child")
                .build();
        schema = Schema.builder()
                .name("root/TestSchema")
                .property("foo", String.class)
                .property("bars", List.class, String.class)
                .property("child", childSchema)
                .property("children", List.class, childSchema)
                .constraint("foo", new NotBlankConstraint())
                .build();
        schemaModel = new SchemaModel()
                .name("root/TestSchema")
                .addAttributesItem(
                        new AttributeDefinitionModel()
                                .name("foo")
                                .type("String")
                                .addConstraintsItem(new NotBlankConstraintModel().constraintType("NotBlank"))
                )
                .addAttributesItem(
                        new AttributeDefinitionModel()
                                .name("bars")
                                .type("List")
                                .contentType("String")
                )
                .addAttributesItem(
                        new AttributeDefinitionModel()
                                .name("child")
                                .type("Entity")
                                .entitySchema("common/Child")
                )
                .addAttributesItem(
                        new AttributeDefinitionModel()
                                .name("children")
                                .type("List")
                                .contentType("Entity")
                                .entitySchema("common/Child")
                );
        schemaRow = SchemaRow.builder()
                .name("root/TestSchema")
                .schemaJson(objectMapper.writeValueAsString(schemaModel))
                .build();
    }

    @Test
    void schemaToSchemaModel() {
        assertEquals(schemaModel, SchemaMapper.INSTANCE.schemaToSchemaModel(schema));
    }

    @Test
    void schemaModelToSchema() {
        assertEquals(schema, SchemaMapper.INSTANCE.schemaModelToSchema(schemaModel));
    }

    @Test
    void schemaRowToSchema() throws JsonProcessingException {
        assertEquals(schema, SchemaMapper.INSTANCE.schemaRowToSchema(schemaRow));
    }

    @Test
    void schemaToSchemaRow() throws JsonProcessingException {
        final SchemaRow actual = SchemaMapper.INSTANCE.schemaToSchemaRow(schema);
        assertEquals(schemaRow.getName(), actual.getName());
        assertEquals(schemaRow.getSchemaJson(), actual.getSchemaJson());
    }
}