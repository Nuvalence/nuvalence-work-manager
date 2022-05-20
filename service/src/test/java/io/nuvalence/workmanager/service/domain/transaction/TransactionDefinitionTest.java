package io.nuvalence.workmanager.service.domain.transaction;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionDefinitionTest {
    private TransactionDefinition transactionDefinition;
    private UUID formId1;
    private UUID formId2;
    private UUID formId3;
    private UUID formId4;

    @BeforeEach
    void setup() {
        formId1 = UUID.fromString("838d7e35-9a13-4983-852b-6a2358b461a2");
        formId2 = UUID.fromString("91d30b0a-f8fa-473a-8556-b6a1f0b86333");
        formId3 = UUID.fromString("349e32eb-b760-4ebb-a89c-edacb4346a91");
        formId4 = UUID.fromString("ddf8754b-dd4a-46e3-bbbc-62d1bc9db4e3");
        transactionDefinition = TransactionDefinition.builder()
                .name("test-transaction")
                .taskFormMappings(
                        List.of(
                                TaskFormMapping.builder()
                                        .formId(formId1)
                                        .taskDefinitionId("target-task")
                                        .build(),
                                TaskFormMapping.builder()
                                        .formId(formId2)
                                        .taskDefinitionId("target-task")
                                        .role("target-role")
                                        .build(),
                                TaskFormMapping.builder()
                                        .formId(formId3)
                                        .taskDefinitionId("target-task")
                                        .role("other-role")
                                        .build(),
                                TaskFormMapping.builder()
                                        .formId(formId4)
                                        .taskDefinitionId("other-task")
                                        .build()
                        )
                )
                .build();
    }

    @Test
    void getFormIdForTaskAndRoleReturnsMatchIncludingRoleIfOneExists() {
        assertEquals(
                Optional.of(formId2),
                transactionDefinition.getFormIdForTaskAndRole("target-task", "target-role")
        );
        assertEquals(
                Optional.of(formId3),
                transactionDefinition.getFormIdForTaskAndRole("target-task", "other-role")
        );
    }

    @Test
    void getFormIdForTaskAndRoleReturnsMatchWithNullRoleIfRoleMatchDoesNotExist() {
        assertEquals(
                Optional.of(formId1),
                transactionDefinition.getFormIdForTaskAndRole("target-task", "yet-another-role")
        );
        assertEquals(
                Optional.of(formId4),
                transactionDefinition.getFormIdForTaskAndRole("other-task", "target-role")
        );
    }

    @Test
    void getFormIdForTaskAndRoleReturnsEmptyIfNoMatchIsSuitable() {
        assertTrue(transactionDefinition.getFormIdForTaskAndRole("yet-another-task", "target-role").isEmpty());
    }

    @Test
    public void equalsHashcodeContract() {
        EqualsVerifier
                .forClass(TransactionDefinition.class)
                .usingGetClass()
                .verify();
    }
}