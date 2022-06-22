package io.nuvalence.workmanager.service.domain.formconfig;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Defines the structure and behavior of a form config type.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "form_config_definition")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
public class FormConfigDefinition {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", length = 1024, nullable = false)
    private String name;

    @Column(name = "schema", nullable = false)
    private String schema;

    @Type(type = "json")
    @Column(name = "form_config_json", nullable = false, columnDefinition = "json")
    private Map<String, Object> formConfigJson;

    @Type(type = "json")
    @Column(name = "renderer_options_json", nullable = true, columnDefinition = "json")
    private Map<String, Object> rendererOptions;

    @Column(name = "description", length = 1024, nullable = true)
    private String description;

    @Column(name = "version", length = 36, nullable = true)
    private String version;

    @Column(name = "status", length = 255, nullable = true)
    private String status;

    @Column(name = "createdby", length = 64, nullable = true)
    private String createdBy;

    @Column(name = "lastupdatedby", length = 64, nullable = true)
    private String lastUpdatedBy;

    @Column(name = "translationrequired", nullable = true)
    private Boolean translationRequired;

    @Column(name = "created_time_stamp", nullable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdTimeStamp;

    @Column(name = "last_updated_time_stamp", nullable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime lastUpdatedTimeStamp;

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormConfigDefinition that = (FormConfigDefinition) o;
        return Objects.equals(name, that.name)
                && Objects.equals(schema, that.schema)
                && Objects.equals(formConfigJson, that.formConfigJson)
                && Objects.equals(rendererOptions, that.rendererOptions)
                && Objects.equals(description, that.description)
                && Objects.equals(version, that.version)
                && Objects.equals(status, that.status)
                && Objects.equals(createdBy, that.createdBy)
                && Objects.equals(lastUpdatedBy, that.lastUpdatedBy)
                && Objects.equals(translationRequired, that.translationRequired)
                && Objects.equals(createdTimeStamp, that.createdTimeStamp)
                && Objects.equals(lastUpdatedTimeStamp, that.lastUpdatedTimeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schema, formConfigJson, rendererOptions, description, version, status,
                createdBy, lastUpdatedBy, translationRequired, createdTimeStamp, lastUpdatedTimeStamp);
    }
}
