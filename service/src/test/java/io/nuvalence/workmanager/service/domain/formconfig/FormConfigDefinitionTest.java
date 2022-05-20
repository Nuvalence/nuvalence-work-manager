package io.nuvalence.workmanager.service.domain.formconfig;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class FormConfigDefinitionTest {

    @Test
    public void equalsHashcodeContract() {
        EqualsVerifier
                .forClass(FormConfigDefinition.class)
                .usingGetClass()
                .verify();
    }
}
