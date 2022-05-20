package io.nuvalence.workmanager.service.domain.dynamicschema;

import io.nuvalence.workmanager.service.domain.dynamicschema.validation.ConstraintViolation;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.LengthConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotBlankConstraint;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTest {
    private Schema contactSchema;
    private Schema addressSchema;
    private Schema emailAddressSchema;
    private Entity contact;

    @BeforeEach
    public void setup() {
        addressSchema = Schema.builder()
                .name("Address")
                .property("line1", String.class)
                .property("line2", String.class)
                .property("city", String.class)
                .property("state", String.class)
                .property("postalCode", String.class)
                .constraint("state", LengthConstraint.builder().min(2).max(2).build())
                .build();
        emailAddressSchema = Schema.builder()
                .name("EmailAddress")
                .property("type", String.class)
                .property("email", String.class)
                .constraint("email", new NotBlankConstraint())
                .build();
        contactSchema = Schema.builder()
                .name("Contact")
                .property("name", String.class)
                .property("address", addressSchema)
                .property("emails", List.class, emailAddressSchema)
                .build();
        contact = new Entity(contactSchema, UUID.randomUUID());
        contact.set("name", "Thomas A. Anderson");
        final Entity address = new Entity(addressSchema);
        address.set("line1", "123 Street St");
        address.set("city", "New York");
        address.set("state", "NY");
        address.set("postalCode", "11111");
        contact.set("address", address);
        final Entity emailAddress1 = new Entity(emailAddressSchema);
        emailAddress1.set("type", "work");
        emailAddress1.set("email", "tanderson@nuvalence.io");
        contact.add("emails", emailAddress1);
    }

    @Test
    void canRetrievePropertyValuesByElPath() {
        assertEquals("Thomas A. Anderson", contact.getProperty("name", String.class));
        assertEquals("New York", contact.getProperty("address.city", String.class));
        assertEquals("tanderson@nuvalence.io", contact.getProperty("emails[0].email", String.class));
    }

    @Test
    void throwsIllegalArgumentExceptionWhenAddIsCalledOnNonList() {
        assertThrows(IllegalArgumentException.class, () -> {
            contact.add("address", contact.getProperty("address", Entity.class));
        }, "IllegalArgumentException was expected");
    }

    @Test
    void throwsIllegalArgumentExceptionExceptionWhenPropertyPathIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            contact.getProperty("faxNumber", String.class);
        }, "IllegalArgumentException was expected");
    }

    @Test
    void validateWillReturnZeroViolationsOnValidEntity() {
        assertTrue(contact.validate().isEmpty());
    }

    @Test
    void validateWillReturnViolationsOnInvalidEntity() {
        // Arrange
        contact.getProperty("address", Entity.class).set("state", "New York");

        // Act
        List<ConstraintViolation> violations = contact.validate();

        // Assert
        assertEquals(1, violations.size());
        assertEquals("address.state", violations.get(0).getPath());
    }

    @Test
    void validateWillSkipValidationOfNullEntityReferences() {
        // Arrange
        contact.set("address", null);

        // Act
        List<ConstraintViolation> violations = contact.validate();

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    public void equalsHashcodeContract() {
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

        EqualsVerifier.forClass(Entity.class)
                .withPrefabValues(DynaClass.class, redDynaClass, blueDynaClass)
                .withPrefabValues(DynaBean.class, redDynaBean, blueDynaBean)
                .withNonnullFields("attributes")
                .suppress(Warning.ALL_FIELDS_SHOULD_BE_USED)
                .verify();
    }
}