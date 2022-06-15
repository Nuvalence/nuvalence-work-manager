package io.nuvalence.workmanager.service.utils.camunda;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.model.bpmn.impl.instance.SequenceFlowImpl;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import java.util.Collection;

/**
 * ExecutionListener for SequenceFlow executions.
 * Can get the status Extension Property from the workflow & assign to DelegateExecution.
 * Retrieve to set the status on a Transaction.
 *
 */
@Slf4j
public class SequenceFlowExecutionListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution delegateExecution) {
        try {
            BpmnModelElementInstance bpmnModelInstance = delegateExecution.getBpmnModelElementInstance();
            SequenceFlowImpl sequenceFlowImpl = bpmnModelInstance
                    .getModelInstance()
                    .getModelElementById(delegateExecution.getCurrentTransitionId());
            ExtensionElements extensionElements = sequenceFlowImpl.getExtensionElements();

            Collection<CamundaProperty> sequenceFlowProperties = extensionElements.getElementsQuery()
                    .filterByType(CamundaProperties.class).singleResult().getCamundaProperties();

            for (CamundaProperty property : sequenceFlowProperties) {
                if (property.getAttributeValue("name").equals("status")) {
                    delegateExecution.setVariable("status", property.getCamundaValue());
                }
                if (property.getAttributeValue("name").equals("publicStatus")) {
                    delegateExecution.setVariable("publicStatus", property.getCamundaValue());
                }
            }
        } catch (Exception e) {
            // if no status set in workflow, currently the status would just remain the same
            log.warn("No status set for SequenceFlow in Camunda workflow");
        }
    }
}