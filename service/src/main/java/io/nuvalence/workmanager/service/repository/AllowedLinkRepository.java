package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.AllowedLink;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for Allowed Links.
 */
public interface AllowedLinkRepository extends CrudRepository<AllowedLink, Integer> {
    @Query("SELECT al FROM AllowedLink al WHERE al.transactionDefinitionKey = :definitionKey")
    List<AllowedLink> findByDefinitionKey(@Param("definitionKey") String definitionKey);
}