package io.nuvalence.workmanager.service.domain.formconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

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

    @Column(name = "form_config_json", nullable = false)
    private String formConfigJson;

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
                && Objects.equals(formConfigJson, that.formConfigJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schema, formConfigJson);
    }
}
