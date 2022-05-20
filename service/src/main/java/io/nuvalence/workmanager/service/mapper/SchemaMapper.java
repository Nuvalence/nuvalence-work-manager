package io.nuvalence.workmanager.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.ConstraintJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaAttributeJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaJson;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow;
import io.nuvalence.workmanager.service.generated.models.AttributeDefinitionModel;
import io.nuvalence.workmanager.service.generated.models.SchemaModel;
import io.nuvalence.workmanager.service.generated.models.ValidationConstraintModel;
import org.apache.commons.beanutils.DynaProperty;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps dynamic schemas between 3 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.SchemaModel})</li>
 *     <li>Logic Object ({@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema})</li>
 *     <li>Persistence Model ({@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow})</li>
 * </ul>
 */
@Mapper(uses = ConstraintMapper.class)
public abstract class SchemaMapper {
    public static final SchemaMapper INSTANCE = Mappers.getMapper(SchemaMapper.class);

    private static final Map<String, Class<?>> SUPPORTED_TYPES = Stream.of(
            String.class,
            List.class,
            Entity.class,
            Boolean.class,
            Integer.class,
            BigDecimal.class,
            LocalDate.class,
            LocalTime.class
    ).collect(Collectors.toMap(Class::getSimpleName, Function.identity()));

    private final ObjectMapper objectMapper;

    public SchemaMapper() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema} to
     * {@link io.nuvalence.workmanager.service.generated.models.SchemaModel}.
     *
     * @param schema Logic model for schema
     * @return API model for schema
     */
    public SchemaModel schemaToSchemaModel(final Schema schema) {
        final SchemaModel model = new SchemaModel();
        model.setName(schema.getName());

        for (DynaProperty property : schema.getDynaProperties()) {
            final AttributeDefinitionModel attribute = new AttributeDefinitionModel();
            attribute.setName(property.getName());
            attribute.setType(typeToString(property.getType()));
            if (property.getContentType() != null) {
                attribute.setContentType(typeToString(property.getContentType()));
            }

            if (Entity.class.equals(property.getType()) || Entity.class.equals(property.getContentType())) {
                attribute.setEntitySchema(schema.getRelatedSchemas().get(property.getName()));
            }

            attribute.setConstraints(
                    schema
                            .getConstraints()
                            .getOrDefault(property.getName(), Collections.emptyList())
                            .stream().map(ConstraintMapper.INSTANCE::constraintToValidationConstraintModel)
                            .collect(Collectors.toList())
            );

            model.getAttributes().add(attribute);
        }

        return model;
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.generated.models.SchemaModel} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema}.
     *
     * @param model API model for schema
     * @return Logic model for schema
     */
    public Schema schemaModelToSchema(final SchemaModel model) {
        final Map<String, String> relatedSchemas = new HashMap<>();
        final Schema.SchemaBuilder builder = Schema.builder();
        builder.name(model.getName());

        for (AttributeDefinitionModel attribute : model.getAttributes()) {
            if (attribute.getContentType() == null) {
                builder.property(attribute.getName(), stringToType(attribute.getType()));
            } else {
                builder.property(
                        attribute.getName(),
                        stringToType(attribute.getType()),
                        stringToType(attribute.getContentType())
                );
            }

            if (attribute.getEntitySchema() != null) {
                relatedSchemas.put(attribute.getName(), attribute.getEntitySchema());
            }

            for (ValidationConstraintModel constraint : attribute.getConstraints()) {
                builder.constraint(
                        attribute.getName(),
                        ConstraintMapper.INSTANCE.validationConstraintModelToConstraint(constraint)
                );
            }
        }
        builder.relatedSchemas(relatedSchemas);

        return builder.build();
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema}.
     *
     * @param row Persistence model for schema
     * @return Logic model for schema
     * @throws JsonProcessingException if schema JSON cannot be parsed
     */
    public Schema schemaRowToSchema(final SchemaRow row) throws JsonProcessingException {
        return schemaJsonToSchema(objectMapper.readValue(row.getSchemaJson(), SchemaJson.class));
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow}.
     *
     * @param schema Logic model for schema
     * @return Persistence model for schema
     * @throws JsonProcessingException if schema cannot be serialized to JSON
     */
    public SchemaRow schemaToSchemaRow(final Schema schema) throws JsonProcessingException {
        return SchemaRow.builder()
                .name(schema.getName())
                .schemaJson(objectMapper.writeValueAsString(schemaToSchemaJson(schema)))
                .build();
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaJson}.
     *
     * @param schema Logic model for schema
     * @return JSON model used in persistence model
     */
    public SchemaJson schemaToSchemaJson(final Schema schema) {
        final SchemaJson json = new SchemaJson();
        json.setName(schema.getName());

        for (DynaProperty property : schema.getDynaProperties()) {
            final SchemaAttributeJson attribute = new SchemaAttributeJson();
            attribute.setName(property.getName());
            attribute.setType(typeToString(property.getType()));
            if (property.getContentType() != null) {
                attribute.setContentType(typeToString(property.getContentType()));
            }

            if (Entity.class.equals(property.getType()) || Entity.class.equals(property.getContentType())) {
                attribute.setEntitySchema(schema.getRelatedSchemas().get(property.getName()));
            }

            attribute.setConstraints(
                    schema
                            .getConstraints()
                            .getOrDefault(property.getName(), Collections.emptyList())
                            .stream().map(ConstraintMapper.INSTANCE::constraintToConstraintJson)
                            .collect(Collectors.toList())
            );

            json.getAttributes().add(attribute);
        }

        return json;
    }

    /**
     * Maps {@link io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaJson} to
     * {@link io.nuvalence.workmanager.service.domain.dynamicschema.Schema}.
     *
     * @param json JSON model from persistence model
     * @return Logic model for schema
     */
    public Schema schemaJsonToSchema(final SchemaJson json) {
        final Map<String, String> relatedSchemas = new HashMap<>();
        final Schema.SchemaBuilder builder = Schema.builder();
        builder.name(json.getName());

        for (SchemaAttributeJson attribute : json.getAttributes()) {
            if (attribute.getContentType() == null) {
                builder.property(attribute.getName(), stringToType(attribute.getType()));
            } else {
                builder.property(
                        attribute.getName(),
                        stringToType(attribute.getType()),
                        stringToType(attribute.getContentType())
                );
            }

            if (attribute.getEntitySchema() != null) {
                relatedSchemas.put(attribute.getName(), attribute.getEntitySchema());
            }

            for (ConstraintJson constraint : attribute.getConstraints()) {
                builder.constraint(
                        attribute.getName(),
                        ConstraintMapper.INSTANCE.constraintJsonToConstraint(constraint)
                );
            }
        }
        builder.relatedSchemas(relatedSchemas);

        return builder.build();
    }

    private String typeToString(final Class<?> type) {
        return type.getSimpleName();
    }

    private Class<?> stringToType(final String typeName) {
        return SUPPORTED_TYPES.get(typeName);
    }

}
