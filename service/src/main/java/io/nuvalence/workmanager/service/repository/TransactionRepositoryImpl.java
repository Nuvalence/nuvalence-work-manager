package io.nuvalence.workmanager.service.repository;

import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.generated.models.TransactionCountByStatusModel;
import io.nuvalence.workmanager.service.models.TransactionStatusCount;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Houses concrete Transaction repository method implementations.
 */
@Repository
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TransactionCountByStatusModel> getTransactionCountsByStatus(Specification<Transaction> specifications) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TransactionStatusCount> query = cb.createQuery(TransactionStatusCount.class);
        Root<Transaction> root = query.from(Transaction.class);
        query.multiselect(root.get("status"), cb.count(root));
        query.select(cb.construct(TransactionStatusCount.class, root.get("status"), cb.count(root)));
        query.where(specifications.toPredicate(root, query, cb));
        query.groupBy(root.get("status"));
        return entityManager.createQuery(query).getResultList().stream().map(c -> {
            TransactionCountByStatusModel count = new TransactionCountByStatusModel();
            count.setCount(c.getCount().intValue());
            count.setStatus(c.getStatus());
            return count;
        }).collect(Collectors.toList());
    }
}
