package io.nuvalence.workmanager.service.delegates;

import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.Objects;
import javax.inject.Named;

/**
 * Delegate class for determining a district.
 */
@RequiredArgsConstructor
@Slf4j
@Named(value = "DetermineDistrictDelegate")
public class DetermineDistrictDelegate implements JavaDelegate {
    private final TransactionService service;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        final Transaction transaction;

        try {
            transaction = service.getTransactionByProcessInstanceId(execution.getProcessInstanceId()).orElse(null);
            if (transaction == null) {
                return;
            }

            Object district = execution.getVariable("district");

            String districtVal = district != null ? district.toString() : null;
            if (!Objects.equals(transaction.getDistrict(), districtVal)) {
                transaction.setDistrict(districtVal);
                service.updateTransaction(transaction);
            }
        } catch (MissingEntityException e) {
            log.error("transaction references missing entity.", e);
        }
    }
}
