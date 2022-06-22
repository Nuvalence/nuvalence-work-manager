package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.TransactionLinkType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repository for Transaction Link Types.
 */

public interface TransactionLinkTypeRepository extends CrudRepository<TransactionLinkType, UUID> {
    @Modifying
    @Query("DELETE FROM TransactionLinkType WHERE id = :id")
    void deleteTransactionLinkTypeById(@Param("id") UUID id);
}
