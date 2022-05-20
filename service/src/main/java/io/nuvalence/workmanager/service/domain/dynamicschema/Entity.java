package io.nuvalence.workmanager.service.domain.dynamicschema;

import io.nuvalence.workmanager.service.domain.dynamicschema.validation.Constraint;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.ConstraintViolation;
import io.nuvalence.workmanager.service.domain.dynamicschema.validation.ValidationContext;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single instance of a data object with a dynamically configured schema.
 */
@ToString
public final class Entity implements DynaBean {
    @Getter
    private final Schema schema;

    @Getter
    private final UUID id;

    @Delegate
    private final DynaBean attributes;

    /**
     * Constructs new Entity with a given schema.
     *
     * @param schema Schema that defines this Entity's structure
     */
    public Entity(final Schema schema) {
        this(schema, null);
    }

    /**
     * Constructs new Entity with a given schema and persistent ID.
     *
     * @param schema Schema that defines this Entity's structure
     * @param id ID to use when persisting this Entity
     */
    public Entity(final Schema schema, final UUID id) {
        this.schema = schema;
        this.id = id;
        try {
            this.attributes = schema.newInstance();
            for (DynaProperty property : schema.getDynaProperties()) {
                if (List.class.isAssignableFrom(property.getType())) {
                    attributes.set(property.getName(), new ArrayList<>());
                }
            }
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to instantiate new " + schema.getName(), e);
        }
    }

    /**
     * Gets property value at a given Expression Language (EL) path and casts it to a provided type.
     *
     * @param path expression Language (EL) path of property to retrieve
     * @param type Class definition of expected property type
     * @param <T> Expected property type
     * @return Property value as type &lt;T&gt;
     */
    public <T> T getProperty(final String path, final Class<T> type) {
        try {
            return type.cast(PropertyUtils.getProperty(attributes, path));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Unable to access path: " + path + " as type " + type.getSimpleName(),
                    e
            );
        }
    }

    /**
     * Adds an element to a list attribute.
     *
     * @param name name of the list attribute
     * @param value value to add
     */
    public void add(final String name, final Object value) {
        if (!isList(name)) {
            throw new IllegalArgumentException(String.format("Field [%s] is not a list.", name));
        }
        @SuppressWarnings("unchecked") final List<Object> list = (List<Object>) get(name);
        list.add(value);
    }

    /**
     * Runs validation checks configured in this Entity's schema.
     *
     * @return List of constraint violations. Empty list if Entity passes validation.
     */
    public List<ConstraintViolation> validate() {
        return validate(new ValidationContext());
    }

    private List<ConstraintViolation> validate(final ValidationContext context) {
        for (String name : schema.getConstraints().keySet()) {
            final Object value = getProperty(name, Object.class);
            context.pushPath(name);

            for (Constraint<?> constraint : schema.getConstraints().get(name)) {
                ((Constraint<Object>) constraint).isValid(value, context);
            }

            context.popPath();
        }

        for (String name : schema.getRelatedSchemas().keySet()) {
            if (isList(name)) {
                validateListOfEntities(name, context);
            } else {
                validateSingleEntity(name, context);
            }
        }

        return context;
    }

    private void validateListOfEntities(String name, ValidationContext context) {
        @SuppressWarnings("unchecked")
        final List<Entity> list = (List<Entity>) getProperty(name, List.class);

        for (int index = 0; index < list.size(); index++) {
            context.pushPath(name + "[" + index + "]");
            list.get(index).validate(context);
            context.popPath();
        }
    }

    private void validateSingleEntity(String name, ValidationContext context) {
        final Entity value = getProperty(name, Entity.class);

        if (value != null) {
            context.pushPath(name);
            value.validate(context);
            context.popPath();
        }
    }

    private boolean isList(final String name) {
        return List.class.isAssignableFrom(schema.getDynaProperty(name).getType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Entity entity = (Entity) o;

        final DynaProperty[] properties = (schema == null) ? new DynaProperty[0] : schema.getDynaProperties();
        for (DynaProperty dynaProperty : properties) {
            if (!Objects.equals(
                    attributes.get(dynaProperty.getName()),
                    entity.attributes.get(dynaProperty.getName())
            )) {
                return false;
            }
        }

        return Objects.equals(schema, entity.schema)
                && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        final int prime = 59;
        int result = 1;
        result = result * prime + Objects.hashCode(id);
        result = result * prime + Objects.hashCode(schema);
        final DynaProperty[] properties = (schema == null) ? new DynaProperty[0] : schema.getDynaProperties();
        for (DynaProperty dynaProperty : properties) {
            result = result * prime + Objects.hashCode(attributes.get(dynaProperty.getName()));
        }

        return result;
    }
}
