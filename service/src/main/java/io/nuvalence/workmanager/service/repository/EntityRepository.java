package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.EntityRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Entities.
 */
public interface EntityRepository extends CrudRepository<EntityRow, UUID> {

    @Query("SELECT er FROM EntityRow er WHERE er.schema = :scehma")
    List<EntityRow> getEntitiesForSchema(@Param("scehma") String schema);

}
