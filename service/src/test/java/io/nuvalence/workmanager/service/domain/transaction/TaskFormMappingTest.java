package io.nuvalence.workmanager.service.domain.transaction;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TaskFormMappingTest {

    @Test
    public void equalsHashcodeContract() {
        EqualsVerifier
                .forClass(TaskFormMapping.class)
                .usingGetClass()
                .verify();
    }
}