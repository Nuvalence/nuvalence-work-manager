package io.nuvalence.workmanager.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.repository.EntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * Service layer to manage Schemas.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class EntityService {
    private final EntityRepository repository;

    private final EntityMapper mapper;

    /**
     * Fetches an entity from the database by id (primary key).
     *
     * @param id entity id to fetch
     * @return entity object
     */
    public Optional<Entity> getEntityById(final UUID id) {
        return repository.findById(id).map(this::mapRowToEntity);
    }

    /**
     * Returns a list of entities that have a given schema.
     *
     * @param schema Name of a schema
     * @return List of entities that have the given schema
     */
    public List<Entity> getEntitiesBySchema(final String schema) {
        return repository.getEntitiesForSchema(schema).stream()
                .map(this::mapRowToEntity)
                .collect(Collectors.toList());
    }

    /**
     * Saves an entity.
     *
     * @param entity entity to save
     * @return post-save version of entity
     */
    public Entity saveEntity(final Entity entity) {
        try {
            final EntityRow saved = repository.save(mapper.entityToEntityRow(entity));
            return mapRowToEntity(saved);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse schema JSON stored in database.", e);
        }
    }

    private Entity mapRowToEntity(final EntityRow row) {
        try {
            return mapper.entityRowToEntity(row);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse schema JSON stored in database.", e);
        } catch (MissingSchemaException e) {
            throw new RuntimeException("Entity referenced missing schema: " + row.getSchema(), e);
        }
    }
}
