package io.nuvalence.workmanager.service.domain.dynamicschema;

import io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dynamically configured schema.
 */
@ToString
public final class Schema implements DynaClass {
    @Delegate
    private final DynaClass dynaClass;

    @Getter
    private final Map<String, String> relatedSchemas;

    @Getter
    private final Map<String, List<Constraint<?>>> constraints;

    /**
     * Constructs new Schema.
     *
     * @param name Name of ths schema
     * @param properties List of property schemas (type and contents)
     * @param relatedSchemas Map of attributes to their named dynamic schemas (for attributes of type Entity)
     * @param constraints Map of attributes to their validation constraints
     */
    @Builder
    public Schema(final String name,
                  final List<DynaProperty> properties,
                  final Map<String, String> relatedSchemas,
                  final Map<String, List<Constraint<?>>> constraints) {
        this.dynaClass = new BasicDynaClass(name, null, properties.toArray(DynaProperty[]::new));
        this.relatedSchemas = relatedSchemas;
        this.constraints = constraints;
    }

    /**
     * Returns true if object under test is equal to this Schema.
     *
     * @param o object to test for equality
     * @return true if object under test is equal to this Schema, false otherwise
     */
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Schema)) {
            return false;
        }

        final Schema other = (Schema) o;

        final DynaProperty[] thisDynaProperties = (this.dynaClass == null)
                ? new DynaProperty[0]
                : this.dynaClass.getDynaProperties();
        final DynaProperty[] otherDynaProperties = (other.dynaClass == null)
                ? new DynaProperty[0]
                : other.dynaClass.getDynaProperties();
        if (!Arrays.equals(thisDynaProperties, otherDynaProperties)) {
            return false;
        }

        if (!Objects.equals(this.relatedSchemas, other.relatedSchemas)) {
            return false;
        }

        return Objects.equals(this.constraints, other.constraints);
    }

    /**
     * Returns a hash code as int that complies with the contract between hashCode() and equals().
     *
     * @return hash code as int
     */
    public int hashCode() {
        final int prime = 59;
        int result = 1;
        final DynaClass $dynaClass = this.dynaClass;
        result = result * prime + ($dynaClass == null ? 43 : Arrays.hashCode($dynaClass.getDynaProperties()));
        final Object $relatedSchemas = this.relatedSchemas;
        result = result * prime + ($relatedSchemas == null ? 43 : $relatedSchemas.hashCode());
        final Object $constraints = this.constraints;
        result = result * prime + ($constraints == null ? 43 : $constraints.hashCode());
        return result;
    }

    /**
     * Fluent builder for Schema instances.
     */
    public static final class SchemaBuilder {
        private List<DynaProperty> properties = new ArrayList<>();
        private Map<String, String> relatedSchemas = new HashMap<>();
        private Map<String, List<Constraint<?>>> constraints = new HashMap<>();

        /**
         * Adds a single property to the collection of properties in this schema with a given name and type.
         *
         * @param name property name
         * @param type property type
         * @return reference to this builder
         */
        public SchemaBuilder property(final String name, final Class<?> type) {
            properties.add(new DynaProperty(name, type));
            return this;
        }

        /**
         * Adds a single property to the collection of properties in this schema with a given name and type.
         *
         * @param name property name
         * @param type Schema defining property type
         * @return reference to this builder
         */
        public SchemaBuilder property(final String name, final Schema type) {
            relatedSchemas.put(name, type.getName());
            properties.add(new DynaProperty(name, Entity.class));
            return this;
        }

        /**
         * Adds a single property to the collection of properties in this schema with a given name and generic type.
         *
         * @param name property name
         * @param type property type (generic)
         * @param contentType property content type
         * @return reference to this builder
         */
        public SchemaBuilder property(final String name, final Class<?> type, final Class<?> contentType) {
            properties.add(new DynaProperty(name, type, contentType));
            return this;
        }

        /**
         * Adds a single property to the collection of properties in this schema with a given name and generic type.
         *
         * @param name property name
         * @param type property type (generic)
         * @param contentType Schema that defines the property content type
         * @return reference to this builder
         */
        public SchemaBuilder property(final String name, final Class<?> type, final Schema contentType) {
            relatedSchemas.put(name, contentType.getName());
            properties.add(new DynaProperty(name, type, Entity.class));
            return this;
        }

        /**
         * Adds a single validation constraint to the schema.
         *
         * @param name property name that constraint applies to
         * @param constraint validation constraint
         * @return reference to this builder
         */
        public SchemaBuilder constraint(final String name, final Constraint<?> constraint) {
            final List<Constraint<?>> constraintList = constraints.computeIfAbsent(name, (key) -> new LinkedList<>());
            constraintList.add(constraint);
            return this;
        }
    }
}
