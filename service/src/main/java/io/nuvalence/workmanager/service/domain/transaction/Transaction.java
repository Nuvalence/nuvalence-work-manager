package io.nuvalence.workmanager.service.domain.transaction;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.service.EntityService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Defines the structure and behavior of a transaction.
 */
@Getter
@NoArgsConstructor
@javax.persistence.Entity
@Table(name = "transaction")
@ToString(exclude = {"data"})
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class Transaction {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transaction_definition_id", length = 36, nullable = false)
    private UUID transactionDefinitionId;

    @Column(name = "transaction_definition_key", length = 255, nullable = false)
    private String transactionDefinitionKey;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_definition_id", nullable = false, insertable = false, updatable = false)
    private TransactionDefinition transactionDefinition;

    @Column(name = "process_instance_id", length = 64, nullable = false)
    private String processInstanceId;

    @Column(name = "entity_id", length = 36, nullable = false)
    private UUID entityId;

    @Setter
    @Column(name = "status", length = 255, nullable = false)
    private String status;

    @Column(name = "created_by", length = 64, nullable = false)
    private String createdBy;

    @Column(name = "subject_user_id", length = 64, nullable = false)
    private String subjectUserId;

    @Setter
    @Column(name = "priority", length = 255)
    private String priority;

    @Setter
    @Column(name = "district", length = 255)
    private String district;

    @Column(name = "created_timestamp", nullable = false)
    private OffsetDateTime createdTimestamp;

    @Column(name = "last_updated_timestamp", nullable = false)
    private OffsetDateTime lastUpdatedTimestamp;

    @Setter
    @Column(name = "assigned_to", length = 64)
    private String assignedTo;

    @Transient
    private transient Entity data;

    /**
     * Constructs a new instance of a Transaction.
     *
     * @param id Transaction ID
     * @param transactionDefinitionId ID for this transactions definition
     * @param transactionDefinitionKey Key for this transactions definition
     * @param processInstanceId ID for Camunda process instance
     * @param entityId Dynamic Entity ID
     * @param status Transaction status
     * @param createdBy User that created the transaction
     * @param subjectUserId User that is the subject of the transaction
     * @param priority Transaction priority
     * @param district Transaction district
     * @param createdTimestamp Timestamp of when transaction was created
     * @param lastUpdatedTimestamp Timestamp of when transaction was last updated
     * @param assignedTo User responsible for the transaction
     */
    @Builder(toBuilder = true)
    public Transaction(UUID id,
                       UUID transactionDefinitionId,
                       String transactionDefinitionKey,
                       String processInstanceId,
                       UUID entityId,
                       String status,
                       String createdBy,
                       String subjectUserId,
                       String priority,
                       String district,
                       OffsetDateTime createdTimestamp,
                       OffsetDateTime lastUpdatedTimestamp,
                       String assignedTo) {
        this.id = id;
        this.transactionDefinitionId = transactionDefinitionId;
        this.transactionDefinitionKey = transactionDefinitionKey;
        this.processInstanceId = processInstanceId;
        this.entityId = entityId;
        this.status = status;
        this.createdBy = createdBy;
        this.subjectUserId = subjectUserId;
        this.priority = priority;
        this.district = district;
        this.createdTimestamp = createdTimestamp;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.assignedTo = assignedTo;
    }

    /**
     * Loads the transient entity reference from configured ID.
     *
     * @param entityService EntityService Bean
     * @throws MissingEntityException If the referenced entity is missing.
     */
    public void loadEntity(final EntityService entityService) throws MissingEntityException {
        data = entityService.getEntityById(entityId).orElseThrow(() -> new MissingEntityException(entityId));
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Transaction that = (Transaction) o;
        return Objects.equals(transactionDefinitionId, that.transactionDefinitionId)
                && Objects.equals(transactionDefinitionKey, that.transactionDefinitionKey)
                && Objects.equals(processInstanceId, that.processInstanceId)
                && Objects.equals(entityId, that.entityId)
                && Objects.equals(status, that.status)
                && Objects.equals(createdBy, that.createdBy)
                && Objects.equals(subjectUserId, that.subjectUserId)
                && Objects.equals(priority, that.priority)
                && Objects.equals(district, that.district)
                && Objects.equals(createdTimestamp, that.createdTimestamp)
                && Objects.equals(lastUpdatedTimestamp, that.lastUpdatedTimestamp)
                && Objects.equals(assignedTo, that.assignedTo)
                && Objects.equals(transactionDefinition, that.transactionDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                transactionDefinitionId,
                transactionDefinitionKey,
                processInstanceId,
                entityId,
                status,
                createdBy,
                subjectUserId,
                priority,
                district,
                createdTimestamp,
                assignedTo,
                lastUpdatedTimestamp,
                transactionDefinition
        );
    }
}
