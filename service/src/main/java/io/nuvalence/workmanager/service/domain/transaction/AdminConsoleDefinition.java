package io.nuvalence.workmanager.service.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Defines the structure and behavior of admin console definition types.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "admin_console_dashboard")
public class AdminConsoleDefinition {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "form_config_id", length = 256)
    private UUID formId;

    @Column(name = "transaction_definition_id", length = 256)
    private UUID transactionDefinitionId;

    @Column(name = "form_name", length = 256)
    private String formName;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "category", length = 256)
    private String category;

    @Column(name = "version", length = 256)
    private String version;

    @Column(name = "status", length = 256)
    private String status;

    @Column(name = "created_time_stamp")
    private OffsetDateTime createdTimeStamp;

    @Column(name = "created_by", length = 256)
    private String createdBy;

    @Column(name = "last_updated_by", length = 250)
    private String lastUpdatedBy;

    @Column(name = "translation_required")
    private Boolean translationRequired;

    @Column(name = "transaction_definition_key", length = 256)
    private String transactionDefinitionKey;

    @Column(name = "last_updated_time_stamp")
    private OffsetDateTime lastUpdatedTimeStamp;

    /**
     * A constructor containing all the allowed values for AdminConsoleDefinition.
     * @param formId The form id of the associated form
     * @param transactionDefinitionId The transaction definition id of the associated form
     * @param formName The name of the associated form
     * @param description The description of the associated form
     * @param category The category of the associated transaction definition
     * @param version The version of the associated form
     * @param status The status of the associated form
     * @param createdTimeStamp The created time stamp of the associated form
     * @param createdBy The user that created the associated form
     * @param lastUpdatedBy The user that last updated the form
     * @param translationRequired The property of the form indicating if additional translations are needed
     * @param transactionDefinitionKey The complaint/application type of the associated transaction definition
     * @param lastUpdatedTimeStamp The last updated time stamp of the associated form
     */
    public AdminConsoleDefinition(UUID formId, UUID transactionDefinitionId, String formName, String description,
                                  String category, String version, String status, OffsetDateTime createdTimeStamp,
                                  String createdBy, String lastUpdatedBy, Boolean translationRequired,
                                  String transactionDefinitionKey, OffsetDateTime lastUpdatedTimeStamp) {
        this.formId = formId;
        this.transactionDefinitionId = transactionDefinitionId;
        this.formName = formName;
        this.description = description;
        this.category = category;
        this.version = version;
        this.status = status;
        this.createdTimeStamp = createdTimeStamp;
        this.createdBy = createdBy;
        this.lastUpdatedBy = lastUpdatedBy;
        this.translationRequired = translationRequired;
        this.transactionDefinitionKey = transactionDefinitionKey;
        this.lastUpdatedTimeStamp = lastUpdatedTimeStamp;
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

        AdminConsoleDefinition that = (AdminConsoleDefinition) o;
        return Objects.equals(id, that.id)
                && Objects.equals(formId, that.formId)
                && Objects.equals(transactionDefinitionId, that.transactionDefinitionId)
                && Objects.equals(formName, that.formName)
                && Objects.equals(description, that.description)
                && Objects.equals(category, that.category)
                && Objects.equals(version, that.version)
                && Objects.equals(status, that.status)
                && Objects.equals(createdTimeStamp, that.createdTimeStamp)
                && Objects.equals(createdBy, that.createdBy)
                && Objects.equals(lastUpdatedBy, that.lastUpdatedBy)
                && Objects.equals(translationRequired, that.translationRequired)
                && Objects.equals(transactionDefinitionKey, that.transactionDefinitionKey)
                && Objects.equals(lastUpdatedTimeStamp, that.lastUpdatedTimeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formId, transactionDefinitionId, formName, description, category, version, status,
                createdTimeStamp, createdBy, lastUpdatedBy, translationRequired, transactionDefinitionKey,
                lastUpdatedTimeStamp);
    }
}
