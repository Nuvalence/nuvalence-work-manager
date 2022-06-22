package io.nuvalence.workmanager.service.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Service layer to manage notifications.
 */
@Component("NotificationServiceDelegate")
public class NotificationServiceDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        // todo implement connection to notification service
    }
}
