package io.nuvalence.workmanager.service.utils.camunda;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.bpmn.listener.ClassDelegateExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

/**
 *  BpmnParseListener for sequence flow executions.
 *
 */
@Slf4j
public class SequenceFlowBpmnParseListener extends AbstractBpmnParseListener implements BpmnParseListener {
    @Override
    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
        super.parseSequenceFlow(sequenceFlowElement, scopeElement, transition);
        try {
            ClassDelegateExecutionListener classDelegateExecutionListener = new ClassDelegateExecutionListener(
                    SequenceFlowExecutionListener.class, null);
            transition.addListener("take", classDelegateExecutionListener);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
