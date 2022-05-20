package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;


class EntityRowTest {

    @Test
    public void equalsHashcodeContract() {
        EqualsVerifier.forClass(EntityRow.class)
                .usingGetClass()
                .verify();
    }
}