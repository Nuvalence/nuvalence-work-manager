package io.nuvalence.workmanager.service.controllers;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.generated.controllers.EntityApiDelegate;
import io.nuvalence.workmanager.service.generated.models.EntityModel;
import io.nuvalence.workmanager.service.mapper.EntityMapper;
import io.nuvalence.workmanager.service.mapper.MissingSchemaException;
import io.nuvalence.workmanager.service.service.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller layer for Entity management.
 */
@Service
@RequiredArgsConstructor
public class EntityApiDelegateImpl implements EntityApiDelegate {
    private final EntityService entityService;
    private final EntityMapper entityMapper;

    @Override
    public ResponseEntity<List<EntityModel>> getEntitiesBySchema(String schema) {
        final List<EntityModel> results = entityService.getEntitiesBySchema(schema).stream()
                .map(entityMapper::entityToEntityModel)
                .collect(Collectors.toList());

        return ResponseEntity.status(200).body(results);
    }

    @Override
    public ResponseEntity<EntityModel> getEntity(UUID id) {
        final Optional<EntityModel> entity = entityService.getEntityById(id).map(entityMapper::entityToEntityModel);

        return (entity.isEmpty())
                ? ResponseEntity.status(404).build()
                : ResponseEntity.status(200).body(entity.get());
    }

    @Override
    public ResponseEntity<EntityModel> postEntity(EntityModel entityModel) {
        try {
            final Entity entity = entityService.saveEntity(entityMapper.entityModelToEntity(entityModel));
            return ResponseEntity.status(200).body(entityMapper.entityToEntityModel(entity));
        } catch (MissingSchemaException e) {
            return ResponseEntity.status(424).build();
        }
    }
}
