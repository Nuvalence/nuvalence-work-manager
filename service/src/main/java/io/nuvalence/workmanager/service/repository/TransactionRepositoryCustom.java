package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.generated.models.TransactionCountByStatusModel;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * To be used for implementing transaction-specific repository methods.
 */
public interface TransactionRepositoryCustom {
    List<TransactionCountByStatusModel> getTransactionCountsByStatus(
            Specification<Transaction> transactionSpecification);
}
