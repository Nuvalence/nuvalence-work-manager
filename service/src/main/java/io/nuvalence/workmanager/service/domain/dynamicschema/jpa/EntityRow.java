package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

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

import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a single row in the dynamic_entity table.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dynamic_entity")

@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
public class EntityRow {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @Column(name = "schema", length = 1024, nullable = false)
    private String schema;

    @Type(type = "json")
    @Column(name = "entity_json", nullable = false, columnDefinition = "json")
    private String entityJson;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityRow entityRow = (EntityRow) o;
        return Objects.equals(schema, entityRow.schema)
                && Objects.equals(entityJson, entityRow.entityJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, entityJson);
    }
}
