package io.nuvalence.workmanager.service.specifications;

import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.domain.transaction.TransactionDefinition;
import io.nuvalence.workmanager.service.models.TransactionFilters;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

/**
 * A helper class for building a query for transactions.
 */
public class TransactionSpecification {
    /**
     * Builds a criteria list based on the filters.
     *
     * @param filters The filters to filter the transactions by
     * @return A list of filtered transactions
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public Specification<Transaction> getTransactions(TransactionFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<TransactionDefinition, Transaction> transactionDefinitionJoin = root
                    .join("transactionDefinition");

            if (StringUtils.isNotBlank(filters.getTransactionDefinitionKey())) {
                predicates.add(criteriaBuilder.equal(root.get("transactionDefinitionKey"),
                        filters.getTransactionDefinitionKey()));
            }

            if (StringUtils.isNotBlank(filters.getCategory())) {
                predicates.add(criteriaBuilder.like(transactionDefinitionJoin.get("category"),
                        filters.getCategory() + "%"));
            }

            if (filters.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdTimestamp"),
                        filters.getStartDate()));
            }

            if (filters.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdTimestamp"),
                        filters.getEndDate()));
            }

            if (filters.getPriority() != null && filters.getPriority().size() > 0) {
                predicates.add(root.get("priority").in(filters.getPriority()));
            }

            if (filters.getStatus() != null && filters.getStatus().size() > 0) {
                predicates.add(root.get("status").in(filters.getStatus()));
            }

            if (filters.getAssignedTo() != null && filters.getAssignedTo().size() > 0) {
                predicates.add(root.get("assignedTo").in(filters.getAssignedTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
