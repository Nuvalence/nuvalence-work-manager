package io.nuvalence.workmanager.service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is a short-lived class used to hold the data for retrieving the count of transactions by status.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionStatusCount {
    private String status;
    private Long count;
}
