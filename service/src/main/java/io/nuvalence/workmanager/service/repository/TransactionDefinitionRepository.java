package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Transaction Definitions.
 */
public interface TransactionDefinitionRepository extends CrudRepository<TransactionDefinition, UUID> {

    @Query("SELECT td FROM TransactionDefinition td")
    List<TransactionDefinition> getAllTransactions();

    @Query("SELECT td FROM TransactionDefinition td WHERE td.name LIKE %:name%")
    List<TransactionDefinition> searchByPartialName(@Param("name") String name);

    @Query("SELECT td FROM TransactionDefinition td WHERE td.category LIKE :category%")
    List<TransactionDefinition> searchByPartialCategory(@Param("category") String category);

    @Query("SELECT td FROM TransactionDefinition td WHERE td.key = :key")
    List<TransactionDefinition> searchByKey(@Param("key") String key);
}
