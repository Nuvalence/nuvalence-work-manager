package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.dynamicschema.Schema;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.LengthConstraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.NotBlankConstraint;
import io.nuvalence.workmanager.service.domain.transaction.MissingEntityException;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.generated.models.TransactionModel;
import io.nuvalence.workmanager.service.service.EntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class TransactionMapperTest {

    @MockBean
    private EntityService entityService;

    private Transaction transaction;
    private TransactionModel model;
    private TransactionMapper mapper;
    private Entity entity;

    @BeforeEach
    void setup() throws MissingEntityException {
        final Schema addressSchema = Schema.builder()
                .name("Address")
                .property("line1", String.class)
                .property("line2", String.class)
                .property("city", String.class)
                .property("state", String.class)
                .property("postalCode", String.class)
                .constraint("state", LengthConstraint.builder().min(2).max(2).build())
                .build();
        final Schema emailAddressSchema = Schema.builder()
                .name("EmailAddress")
                .property("type", String.class)
                .property("email", String.class)
                .constraint("email", new NotBlankConstraint())
                .build();
        final Schema contactSchema = Schema.builder()
                .name("Contact")
                .property("name", String.class)
                .property("address", addressSchema)
                .property("emails", List.class, emailAddressSchema)
                .property("tags", List.class, String.class)
                .build();
        entity = new Entity(contactSchema, UUID.randomUUID());
        entity.set("name", "Thomas A. Anderson");
        entity.set("tags", List.of("tag1", "tag2"));
        final Entity address = new Entity(addressSchema);
        address.set("line1", "123 Street St");
        address.set("city", "New York");
        address.set("state", "NY");
        address.set("postalCode", "11111");
        entity.set("address", address);
        final Entity emailAddress1 = new Entity(emailAddressSchema);
        emailAddress1.set("type", "work");
        emailAddress1.set("email", "tanderson@nuvalence.io");
        entity.add("emails", emailAddress1);

        transaction = Transaction.builder()
                .transactionDefinitionKey("test")
                .transactionDefinitionId(UUID.randomUUID())
                .entityId(UUID.randomUUID())
                .processInstanceId("process-id")
                .createdBy("Dummy user")
                .assignedTo("Dummy Agent")
                .createdTimestamp(OffsetDateTime.now())
                .lastUpdatedTimestamp(OffsetDateTime.now())
                .status("new")
                .priority("low")
                .district("DISTRICT1")
                .build();
        Mockito.when(entityService.getEntityById(transaction.getEntityId())).thenReturn(Optional.of(entity));
        transaction.loadEntity(entityService);
        model = new TransactionModel()
                .transactionDefinitionKey("test")
                .transactionDefinitionId(transaction.getTransactionDefinitionId())
                .processInstanceId("process-id")
                .createdBy("Dummy user")
                .assignedTo("Dummy Agent")
                .priority("low")
                .district("DISTRICT1")
                .createdTimestamp(transaction.getCreatedTimestamp())
                .lastUpdatedTimestamp(transaction.getLastUpdatedTimestamp())
                .status("new")
                .putDataItem("attribute", "value");
        mapper = Mappers.getMapper(TransactionMapper.class);
        final EntityMapper entityMapper = Mappers.getMapper(EntityMapper.class);
        mapper.setEntityMapper(entityMapper);
        model.setData(entityMapper.convertAttributesToGenericMap(entity));
    }

    @Test
    void transactionToTransactionModel() {
        assertEquals(model, mapper.transactionToTransactionModel(transaction));
    }

    @Test
    void entityToPropertyPathMap() {
        final Map<String, Object> expected = Map.of(
                "name", "Thomas A. Anderson",
                "tags[0]", "tag1",
                "tags[1]", "tag2",
                "address.line1", "123 Street St",
                "address.city", "New York",
                "address.state", "NY",
                "address.postalCode", "11111",
                "emails[0].type", "work",
                "emails[0].email", "tanderson@nuvalence.io"
        );

        assertEquals(expected, mapper.entityToPropertyPathMap(entity));
    }

}
