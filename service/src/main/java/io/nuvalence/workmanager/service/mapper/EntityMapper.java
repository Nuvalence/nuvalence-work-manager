package io.nuvalence.workmanager.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow;
import io.nuvalence.workmanager.service.generated.models.EntityModel;
import io.nuvalence.workmanager.service.service.SchemaService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.DynaProperty;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps dynamic entities between 3 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.EntityModel})</li>
 *     <li>Logic Object ({@link io.nuvalence.workmanager.service.domain.dynamicschema.Entity})</li>
 *     <li>Persistence Model ({@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow})</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
@Slf4j
public abstract class EntityMapper {
    public static final EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    private final ObjectMapper objectMapper;

    @Autowired
    @Setter
    private SchemaService schemaService;

    /**
     * Constructs a new instance of an EntityMapper.
     */
    public EntityMapper() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.Entity} to
     * {@link io.nuvalence.workmanager.service.generated.models.EntityModel}.
     *
     * @param entity Logic model for entity
     * @return API model for entity
     */
    public EntityModel entityToEntityModel(final Entity entity) {
        return new EntityModel()
                .id(entity.getId())
                .schema(entity.getSchema().getName())
                .data(convertAttributesToGenericMap(entity));
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.generated.models.EntityModel} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.Entity}.
     *
     * @param model API model for entity
     * @return Logic model for entity
     * @throws MissingSchemaException If the model references a schema that does not exist in the system.
     */
    public Entity entityModelToEntity(final EntityModel model) throws MissingSchemaException {
        final Schema schema = schemaService.getSchemaByName(model.getSchema())
                .orElseThrow(() -> new MissingSchemaException(model.getSchema()));
        final Entity entity = new Entity(schema, model.getId());
        applyMappedPropertiesToEntity(entity, model.getData());

        return entity;
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.Entity} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow}.
     *
     * @param entity Logic model for entity
     * @return Persistence model for entity
     * @throws JsonProcessingException if an exception occurs writing entity data to JSON.
     */
    public EntityRow entityToEntityRow(final Entity entity) throws JsonProcessingException {
        final Map<String, Object> data = convertAttributesToGenericMap(entity);

        return EntityRow.builder()
                .id(entity.getId())
                .schema(entity.getSchema().getName())
                .entityJson(objectMapper.writeValueAsString(data))
                .build();
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.Entity}.
     *
     * @param row Persistence model for entity
     * @return Logic model for entity
     * @throws MissingSchemaException If the model references a schema that does not exist in the system.
     * @throws JsonProcessingException if an exception occurs reading entity data from JSON.
     */
    public Entity entityRowToEntity(final EntityRow row) throws JsonProcessingException, MissingSchemaException {
        final Schema schema = schemaService.getSchemaByName(row.getSchema())
                .orElseThrow(() -> new MissingSchemaException(row.getSchema()));
        final Entity entity = new Entity(schema, row.getId());
        final Map<String, Object> data = objectMapper.readValue(
                row.getEntityJson(),
                new TypeReference<Map<String, Object>>() {}
        );
        applyMappedPropertiesToEntity(entity, data);

        return entity;
    }

    /**
     * Produces a generic map, suitable for JSON serialization.
     *
     * @param entity Entity to convert ot a map
     * @return A generic map (string keys, any values)
     */
    public Map<String, Object> convertAttributesToGenericMap(final Entity entity) {
        final Map<String, Object> attributes = new HashMap<>();
        for (DynaProperty dynaProperty : entity.getSchema().getDynaProperties()) {
            final Object value = entity.get(dynaProperty.getName());
            if (value != null) {
                attributes.put(dynaProperty.getName(), convertDynaPropertyValueToGenericObject(value));
            }
        }

        return attributes;
    }

    /**
     * Applies data to an entity. This method is suitable for handling partial updates as it will not remove attribute
     * values from the target Entity that are missing from the data map.
     *
     * @param entity Entity to update.
     * @param data Generic map of data to apply to entity.
     * @throws MissingSchemaException If the entity's schema references a child schema that does not exist in the
     *                                system.
     */
    public void applyMappedPropertiesToEntity(final Entity entity,
                                              final Map<String, Object> data) throws MissingSchemaException {
        final Schema schema = entity.getSchema();

        for (DynaProperty dynaProperty : schema.getDynaProperties()) {
            final String attributeName = dynaProperty.getName();
            if (data.containsKey(attributeName)) {
                final Object value = data.get(attributeName);
                log.debug(
                        "{}.{}({}) = {}",
                        schema.getName(), dynaProperty.getName(), dynaProperty.getType().getSimpleName(),
                        value.toString()
                );
                if (List.class.isAssignableFrom(dynaProperty.getType())) {
                    @SuppressWarnings("unchecked")
                    final List<?> list = convertListValueToEntity(schema, attributeName, (List<Object>) value);
                    entity.set(attributeName, list);
                } else {
                    entity.set(
                            attributeName,
                            convertSingleValueToEntity(schema, dynaProperty.getType(), attributeName, value)
                    );
                }
            }
        }
    }

    private Object convertDynaPropertyValueToGenericObject(final Object object) {
        if (object instanceof Entity) {
            return convertAttributesToGenericMap((Entity) object);
        } else if (List.class.isAssignableFrom(object.getClass())) {
            return ((List<?>) object).stream()
                    .map(this::convertDynaPropertyValueToGenericObject)
                    .collect(Collectors.toList());
        }

        return object;
    }

    private Object convertSingleValueToEntity(final Schema schema,
                                              final Class<?> type,
                                              final String key,
                                              final Object value) throws MissingSchemaException {
        if (Entity.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            final EntityModel subModel = new EntityModel()
                    .schema(schema.getRelatedSchemas().get(key))
                    .data((Map<String,Object>) value);
            return entityModelToEntity(subModel);
        }

        return value;
    }

    private List<?> convertListValueToEntity(final Schema schema,
                                             final String key,
                                             final List<Object> list) throws MissingSchemaException {
        final List<Object> result = new LinkedList<>();
        for (Object value : list) {
            result.add(convertSingleValueToEntity(
                    schema,
                    schema.getDynaProperty(key).getContentType(),
                    key,
                    value
            ));
        }

        return result;
    }
}
