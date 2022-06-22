package io.nuvalence.workmanager.service.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Service layer to manage document / license generation.
 */
@Component("DocumentServiceDelegate")
public class DocumentServiceDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // todo implement document / license generation & upload
    }
}
