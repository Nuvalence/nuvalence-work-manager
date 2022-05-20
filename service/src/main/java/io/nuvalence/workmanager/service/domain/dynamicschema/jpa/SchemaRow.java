package io.nuvalence.workmanager.service.domain.dynamicschema.jpa;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "dynamic_schema")
@ToString

@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})

public class SchemaRow {
    @Id
    @Column(name = "name", length = 1024, nullable = false)
    private String name;

    @Type(type = "json")
    @Column(name = "schema_json", nullable = false, columnDefinition = "json")
    private String schemaJson;
}
