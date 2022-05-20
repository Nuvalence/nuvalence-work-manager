package io.nuvalence.workmanager.service.domain.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Defines the structure and behavior of a transaction type.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction_definition")
public class TransactionDefinition {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "transaction_definition_key", length = 255, nullable = false)
    private String key;

    @Column(name = "name", length = 1024, nullable = false)
    private String name;

    @Column(name = "category", length = 1024, nullable = true)
    private String category;

    @Column(name = "process_definition_key", length = 255, nullable = false)
    private String processDefinitionKey;

    @Column(name = "entity_schema", length = 1024, nullable = false)
    private String entitySchema;

    @Column(name = "default_status", length = 255, nullable = false)
    private String defaultStatus;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "transaction_definition_id", nullable = false)
    @Builder.Default
    private List<TaskFormMapping> taskFormMappings = new LinkedList<>();

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "transaction_definition_id", nullable = false)
    @Builder.Default
    private List<NamedFormMapping> namedFormMappings = new LinkedList<>();

    /**
     * Searches for a TaskFormMapping for the given taskDefinitionId and role. Prioritizing an exact match before
     * matching an entry with an unspecified role.
     *
     * @param taskDefinitionId Task definition to find form for
     * @param role User role to find form for
     * @return ID of form as UUID
     */
    public Optional<UUID> getFormIdForTaskAndRole(final String taskDefinitionId, final String role) {
        return taskFormMappings.stream()
                .filter(mapping ->  mapping.getTaskDefinitionId().equals(taskDefinitionId)
                        && Objects.equals(mapping.getRole(), role))
                .findAny()
                .or(() -> taskFormMappings.stream()
                        .filter(mapping -> mapping.getTaskDefinitionId().equals(taskDefinitionId)
                                && mapping.getRole() == null)
                        .findAny()
                ).map(TaskFormMapping::getFormId);
    }

    /**
     * Searched for a NamedFormMapping for the given role. Prioritizing an exact match before
     * matching an entry with an unspecified role.
     * @param role User role to find form for
     * @return name of form
     */
    public Optional<String> getFormNameForRole(final String role) {
        return namedFormMappings.stream()
                .filter(mapping -> Objects.equals(mapping.getRole(), role))
                .findAny()
                .or(() -> namedFormMappings.stream()
                        .filter(mapping -> mapping.getRole() == null)
                        .findAny()
                ).map(NamedFormMapping::getFormConfigName);
    }

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }

        TransactionDefinition that = (TransactionDefinition) o;
        return Objects.equals(name, that.name)
                && Objects.equals(key, that.key)
                && Objects.equals(processDefinitionKey, that.processDefinitionKey)
                && Objects.equals(entitySchema, that.entitySchema)
                && Objects.equals(taskFormMappings, that.taskFormMappings)
                && Objects.equals(namedFormMappings, that.namedFormMappings)
                && Objects.equals(defaultStatus, that.defaultStatus)
                && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, key, processDefinitionKey, entitySchema, taskFormMappings,
                namedFormMappings,defaultStatus, category);
    }
}
