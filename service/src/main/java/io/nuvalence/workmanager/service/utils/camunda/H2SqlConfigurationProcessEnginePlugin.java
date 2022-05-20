package io.nuvalence.workmanager.service.utils.camunda;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ProcessEnginePlugin to overcome Camunda incompatibility with H2 version 2.+.
 *
 * @see <a href="https://forum.camunda.org/t/camunda-not-compatible-with-h2-2-0-202/32250/4">Issue in Forum</a>
 */
@Component
@Order(Ordering.DEFAULT_ORDER + 2)
public class H2SqlConfigurationProcessEnginePlugin implements ProcessEnginePlugin {

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        DbSqlSessionFactory.databaseSpecificTrueConstant.put("h2", "true");
        DbSqlSessionFactory.databaseSpecificFalseConstant.put("h2", "false");
        DbSqlSessionFactory.databaseSpecificBitAnd2.put("h2", ",CAST(");
        DbSqlSessionFactory.databaseSpecificBitAnd3.put("h2", " AS BIGINT))");
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
