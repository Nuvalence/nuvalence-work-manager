package io.nuvalence.workmanager.service.domain.transaction;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.service.EntityService;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class TransactionTest {

    @MockBean
    private EntityService entityService;

    @Test
    void loadEntity() throws MissingEntityException {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .entityId(UUID.randomUUID())
                .build();
        final Schema schema = Schema.builder()
                .name("testschema")
                .property("attribute", String.class)
                .build();
        final Entity entity = new Entity(schema, UUID.randomUUID());
        Mockito.when(entityService.getEntityById(transaction.getEntityId())).thenReturn(Optional.of(entity));

        // Act
        transaction.loadEntity(entityService);

        // Assert
        Assertions.assertEquals(entity, transaction.getData());
    }

    @Test
    void loadEntityWhenNotFound() {
        // Arrange
        final Transaction transaction = Transaction.builder()
                .entityId(UUID.randomUUID())
                .build();
        Mockito.when(entityService.getEntityById(transaction.getEntityId())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(MissingEntityException.class, () -> transaction.loadEntity(entityService));
    }

    @Test
    public void equalsHashcodeContract() {
        // we shouldn't need to mock this, since we tell EqualsVerifier to ignore data, but there appears to be a bug.
        final BasicDynaClass redDynaClass = new BasicDynaClass(
                "redschema",
                BasicDynaBean.class,
                List.of(
                        new DynaProperty("foo", String.class),
                        new DynaProperty("bar", List.class, String.class),
                        new DynaProperty("baz", Integer.class)
                ).toArray(new DynaProperty[0])
        );
        final BasicDynaClass blueDynaClass = new BasicDynaClass(
                "blueschema",
                BasicDynaBean.class,
                List.of(
                        new DynaProperty("foo", String.class),
                        new DynaProperty("baz", String.class)
                ).toArray(new DynaProperty[0])
        );
        final BasicDynaBean redDynaBean = new BasicDynaBean(redDynaClass);
        redDynaBean.set("foo", "foo");
        redDynaBean.set("bar", List.of("bar"));
        redDynaBean.set("baz", 42);
        final BasicDynaBean blueDynaBean = new BasicDynaBean(redDynaClass);
        blueDynaBean.set("foo", "foo");
        blueDynaBean.set("bar", List.of("baz"));

        EqualsVerifier
                .forClass(Transaction.class)
                .withIgnoredFields("data")
                .withPrefabValues(DynaClass.class, redDynaClass, blueDynaClass)
                .withPrefabValues(DynaBean.class, redDynaBean, blueDynaBean)
                .usingGetClass()
                .verify();
    }
}