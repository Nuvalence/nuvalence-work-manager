package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.formconfig.FormConfigDefinition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction Definitions.
 */
public interface FormConfigRepository extends CrudRepository<FormConfigDefinition, UUID> {

    @Query("SELECT fic FROM FormConfigDefinition fic WHERE fic.name LIKE %:query%")
    List<FormConfigDefinition> searchByPartialName(@Param("query") String query);

    @Query("SELECT fic from FormConfigDefinition fic WHERE fic.name = :name")
    Optional<FormConfigDefinition> searchByName(@Param("name") String name);
}
