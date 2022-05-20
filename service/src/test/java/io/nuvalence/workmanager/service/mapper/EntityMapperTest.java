package io.nuvalence.workmanager.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.LengthConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotBlankConstraint;
import io.nuvalence.workmanager.service.generated.models.EntityModel;
import io.nuvalence.workmanager.service.service.SchemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {
    @Mock
    private SchemaService schemaService;
    private EntityMapper mapper;
    private Entity entity;
    private EntityModel model;
    private EntityRow row;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws JsonProcessingException {
        final UUID entityId = UUID.randomUUID();
        final Schema addressSchema = Schema.builder()
                .name("Address")
                .property("line1", String.class)
                .property("line2", String.class)
                .property("city", String.class)
                .property("state", String.class)
                .property("postalCode", String.class)
                .constraint("state", LengthConstraint.builder().min(2).max(2).build())
                .build();
        final Schema emailAddressSchema = Schema.builder()
                .name("EmailAddress")
                .property("type", String.class)
                .property("email", String.class)
                .constraint("email", new NotBlankConstraint())
                .build();
        final Schema contactSchema = Schema.builder()
                .name("Contact")
                .property("name", String.class)
                .property("address", addressSchema)
                .property("emails", List.class, emailAddressSchema)
                .build();
        entity = new Entity(contactSchema, entityId);
        entity.set("name", "Thomas A. Anderson");
        final Entity address = new Entity(addressSchema);
        address.set("line1", "123 Street St");
        address.set("city", "New York");
        address.set("state", "NY");
        address.set("postalCode", "11111");
        entity.set("address", address);
        final Entity emailAddress = new Entity(emailAddressSchema);
        emailAddress.set("type", "work");
        emailAddress.set("email", "tanderson@nuvalence.io");
        entity.add("emails", emailAddress);

        model = new EntityModel()
                .id(entityId)
                .schema("Contact")
                .data(Map.of(
                        "name", "Thomas A. Anderson",
                        "address", Map.of(
                                "line1", "123 Street St",
                                "city", "New York",
                                "state", "NY",
                                "postalCode", "11111"
                        ),
                        "emails", List.of(Map.of(
                                "type", "work",
                                "email", "tanderson@nuvalence.io"
                        ))
                ));

        objectMapper = new ObjectMapper();
        row = EntityRow.builder()
                .id(entityId)
                .schema("Contact")
                .entityJson(objectMapper.writeValueAsString(Map.of(
                        "name", "Thomas A. Anderson",
                        "address", Map.of(
                                "line1", "123 Street St",
                                "city", "New York",
                                "state", "NY",
                                "postalCode", "11111"
                        ),
                        "emails", List.of(Map.of(
                                "type", "work",
                                "email", "tanderson@nuvalence.io"
                        ))
                )))
                .build();

        mapper = Mappers.getMapper(EntityMapper.class);
        mapper.setSchemaService(schemaService);

        Mockito.lenient()
                .when(schemaService.getSchemaByName("Address"))
                .thenReturn(Optional.of(addressSchema));
        Mockito.lenient()
                .when(schemaService.getSchemaByName("EmailAddress"))
                .thenReturn(Optional.of(emailAddressSchema));
        Mockito.lenient()
                .when(schemaService.getSchemaByName("Contact"))
                .thenReturn(Optional.of(contactSchema));
    }

    @Test
    void entityToEntityModel() {
        assertEquals(model, mapper.entityToEntityModel(entity));
    }

    @Test
    void entityModelToEntity() throws MissingSchemaException {
        assertEquals(entity, mapper.entityModelToEntity(model));
    }

    @Test
    void entityModelToEntityThrowsMissingSchemaExceptionWhenSchemaDoesntExist() {
        // Arrange
        final EntityModel entityModel = new EntityModel()
                .schema("Missing")
                .data(Map.of("foo", "bar"));
        Mockito.when(schemaService.getSchemaByName("Missing")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(MissingSchemaException.class, () -> mapper.entityModelToEntity(entityModel));
    }

    @Test
    void entityToEntityRow() throws JsonProcessingException {
        assertEntityRowsEqual(row, mapper.entityToEntityRow(entity));
    }

    @Test
    void entityRowToEntity() throws JsonProcessingException, MissingSchemaException {
        assertEquals(entity, mapper.entityRowToEntity(row));
    }

    @Test
    void entityRowToEntityThrowsMissingSchemaExceptionWhenSchemaDoesntExist() throws JsonProcessingException {
        // Arrange
        final EntityRow entityRow = EntityRow.builder()
                .schema("Missing")
                .entityJson(objectMapper.writeValueAsString(Map.of("foo", "bar")))
                .build();
        Mockito.when(schemaService.getSchemaByName("Missing")).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(MissingSchemaException.class, () -> mapper.entityRowToEntity(entityRow));
    }

    private void assertEntityRowsEqual(EntityRow expected, EntityRow actual) throws JsonProcessingException {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getSchema(), actual.getSchema());
        assertEquals(
                objectMapper.readValue(expected.getEntityJson(), Map.class),
                objectMapper.readValue(actual.getEntityJson(), Map.class)
        );
    }
}