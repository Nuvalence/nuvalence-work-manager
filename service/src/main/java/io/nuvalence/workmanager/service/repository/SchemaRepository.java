package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.dynamicschema.jpa.SchemaRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for Schemas.
 */
public interface SchemaRepository extends CrudRepository<SchemaRow, String> {

    @Query("SELECT s FROM SchemaRow s WHERE s.name LIKE %:query%")
    List<SchemaRow> searchByPartialName(@Param("query") String query);
}
