package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.repository.EntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EntityServiceTest {

    @Mock
    private EntityRepository repository;
    private EntityService service;
    private EntityMapper mapper;
    @Mock
    private SchemaService schemaService;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(EntityMapper.class);
        mapper.setSchemaService(schemaService);
        service = new EntityService(repository, mapper);
    }

    @Test
    void getEntityByIdReturnsEntityIfFound() throws JsonProcessingException {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity = new Entity(schema, UUID.randomUUID());
        final EntityRow row = mapper.entityToEntityRow(entity);
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(repository.findById(entity.getId())).thenReturn(Optional.ofNullable(row));

        // Act and Assert
        assertEquals(Optional.of(entity), service.getEntityById(entity.getId()));
    }

    @Test
    void getEntityByIdReturnsEmptyOptionalIfNotFound() throws JsonProcessingException {
        // Arrange
        final UUID id = UUID.randomUUID();
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertEquals(Optional.empty(), service.getEntityById(id));
    }

    @Test
    void getEntityByIdThrowsRuntimeExceptionWhenJsonCannotBeParsed() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final EntityRow row = EntityRow.builder()
                .id(id)
                .schema(schema.getName())
                .entityJson("Not JSON")
                .build();
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(row));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> service.getEntityById(id));
    }

    @Test
    void getEntityByIdThrowsRuntimeExceptionWhenSchemaCantBeFound() {
        // Arrange
        final UUID id = UUID.randomUUID();
        final EntityRow row = EntityRow.builder()
                .id(id)
                .schema("notfound")
                .build();
        Mockito.when(schemaService.getSchemaByName(row.getSchema())).thenReturn(Optional.empty());
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(row));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> service.getEntityById(id));
    }

    @Test
    void getEntitiesBySchemaReturnsFoundEntities() throws JsonProcessingException {
        // Arrange
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity1 = new Entity(schema, UUID.randomUUID());
        final Entity entity2 = new Entity(schema, UUID.randomUUID());
        final EntityRow row1 = mapper.entityToEntityRow(entity1);
        final EntityRow row2 = mapper.entityToEntityRow(entity2);
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(repository.getEntitiesForSchema(schema.getName())).thenReturn(List.of(row1, row2));

        // Act and Assert
        assertEquals(List.of(entity1, entity2), service.getEntitiesBySchema(schema.getName()));
    }

    @Test
    void saveEntityReturnsEntityBasedOnPostSavePersistenceModel() throws JsonProcessingException {
        // Arrange
        final UUID id = UUID.randomUUID();
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity = new Entity(schema);
        final EntityRow row = mapper.entityToEntityRow(entity);
        final Entity finalEntity = new Entity(schema, id);
        final EntityRow finalRow = mapper.entityToEntityRow(finalEntity);
        Mockito.when(schemaService.getSchemaByName(schema.getName())).thenReturn(Optional.of(schema));
        Mockito.when(repository.save(row)).thenReturn(finalRow);

        // Act and Assert
        assertEquals(finalEntity, service.saveEntity(entity));
    }
}