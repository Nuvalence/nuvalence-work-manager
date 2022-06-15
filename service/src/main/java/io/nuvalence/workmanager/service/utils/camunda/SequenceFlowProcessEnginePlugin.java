package io.nuvalence.workmanager.service.utils.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ProcessEnginePlugin for attaching the SequenceFlowBpmnParseListener to the Camunda process engine.
 *
 */
@Component
public class SequenceFlowProcessEnginePlugin implements ProcessEnginePlugin {
    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        List<BpmnParseListener> postParseListeners = processEngineConfiguration.getCustomPostBPMNParseListeners();
        if (postParseListeners == null) {
            postParseListeners = new ArrayList<>();
            processEngineConfiguration.setCustomPostBPMNParseListeners(postParseListeners);
        }
        postParseListeners.add(new SequenceFlowBpmnParseListener());
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // in this case only preInit is necessary
    }

    @Override
    public void postProcessEngineBuild(ProcessEngine processEngine) {
        // in this case only preInit is necessary
    }
}
