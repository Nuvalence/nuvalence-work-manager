package io.nuvalence.workmanager.service.domain.dynamicschema;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaTest {
    @Test
    public void equalsHashcodeContract() {
        EqualsVerifier.forClass(Schema.class)
                .withPrefabValues(
                        DynaClass.class,
                        new BasicDynaClass(
                                "redschema",
                                BasicDynaBean.class,
                                List.of(
                                        new DynaProperty("foo", String.class),
                                        new DynaProperty("bar", List.class, String.class),
                                        new DynaProperty("baz", Integer.class)
                                ).toArray(new DynaProperty[0])
                        ),
                        new BasicDynaClass(
                                "blueschema",
                                BasicDynaBean.class,
                                List.of(
                                        new DynaProperty("foo", String.class),
                                        new DynaProperty("baz", String.class)
                                ).toArray(new DynaProperty[0])
                        )
                )
                .verify();
    }
}