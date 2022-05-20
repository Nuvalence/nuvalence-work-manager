package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Transactions.
 */
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

    // TODO When we implement versioned transaction configuration, this will need to sort results
    @Query("SELECT t FROM Transaction t WHERE t.transactionDefinitionKey = :key")
    List<Transaction> searchByTransactionDefinitionKey(@Param("key") String key);

    @Query("SELECT t FROM Transaction t, TransactionDefinition td WHERE t.transactionDefinitionId = td.id "
            + "AND td.category LIKE :category%")
    List<Transaction> searchByCategory(@Param("category") String category);

    @Query("SELECT t FROM Transaction t WHERE t.createdBy = :userId")
    List<Transaction> searchByTransactionByUser(@Param("userId") String userId);
}
