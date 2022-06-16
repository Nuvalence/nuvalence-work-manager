package io.nuvalence.workmanager.service.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The filters to filter the transactions by.
 */
@Getter
@Setter
public class TransactionFilters extends BaseFilters {
    private String transactionDefinitionKey;
    private String category;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private List<String> priority;
    private List<String> status;
    private List<String> assignedTo;
    private Boolean assignedToMe;

    /**
     * Builder for TransactionFilters.
     *
     * @param transactionDefinitionKey The transaction definition key
     * @param category The transaction category
     * @param startDate The start date to filter transactions by
     * @param endDate The end date to filter transactions by
     * @param priority The priority to filter transactions by
     * @param status The status to filter transactions by
     * @param assignedTo The assigned user to filter transactions by
     * @param assignedToMe Filter transactions assigned to yourself
     * @param sortCol The column to filter transactions by
     * @param sortDir The direction to filter transactions by
     * @param pageNumber The number of the pages to get transactions
     * @param pageSize The number of transactions per page
     */
    @Builder
    public TransactionFilters(String transactionDefinitionKey, String category,
                              OffsetDateTime startDate, OffsetDateTime endDate,
                              List<String> priority, List<String> status, List<String> assignedTo, Boolean assignedToMe,
                              String sortCol, String sortDir,
                              Integer pageNumber, Integer pageSize) {
        super(sortCol, sortDir, pageNumber, pageSize);
        this.transactionDefinitionKey = transactionDefinitionKey;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.status = status;
        this.assignedTo = assignedTo;
        this.assignedToMe = assignedToMe;
    }
}
