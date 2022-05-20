package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.domain.dynamicschema.Entity;
import io.nuvalence.workmanager.service.domain.transaction.Transaction;
import io.nuvalence.workmanager.service.generated.models.TransactionModel;
import lombok.Setter;
import org.apache.commons.beanutils.DynaProperty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Maps transactions between the following 2 forms.
 *
 * <ul>
 *     <li>API Model ({@link io.nuvalence.workmanager.service.generated.models.TransactionModel})</li>
 *     <li>Logic/Persistence Model ({@link io.nuvalence.workmanager.service.domain.transaction.Transaction})</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public abstract class TransactionMapper {

    @Autowired
    @Setter
    protected EntityMapper entityMapper;

    @Mapping(target = "data", expression = "java(entityMapper.convertAttributesToGenericMap(transaction.getData()))")
    public abstract TransactionModel transactionToTransactionModel(Transaction transaction);

    /**
     * Converts an entity to a flat map of property paths (EL language) to values.
     *
     * @param entity source data for mapping
     * @return Flat map of property paths (EL language) to values
     */
    public Map<String, Object> entityToPropertyPathMap(Entity entity) {
        final Map<String, Object> map = new HashMap<>();
        copyEntityAttributesToPropertyPathMap(new Stack<>(), entity, map);
        return map;
    }

    private static void copyEntityAttributesToPropertyPathMap(final Stack<String> path,
                                                              final Entity entity,
                                                              final Map<String, Object> map) {
        for (DynaProperty property : entity.getDynaClass().getDynaProperties()) {
            final String key = property.getName();
            final Object value = entity.get(key);

            if (value != null) {
                if (List.class.isAssignableFrom(value.getClass())) {
                    @SuppressWarnings("unchecked")
                    final List<Object> list = (List<Object>) value;
                    for (int i = 0; i < list.size(); i++) {
                        final String indexedKey = key + "[" + i + "]";
                        final Object item = list.get(i);
                        if (Entity.class.isAssignableFrom(property.getContentType())) {
                            path.push(indexedKey);
                            copyEntityAttributesToPropertyPathMap(path, (Entity) item, map);
                            path.pop();
                        } else {
                            path.push(indexedKey);
                            map.put(String.join(".", path), item);
                            path.pop();
                        }
                    }
                } else if (Entity.class.isAssignableFrom(property.getType())) {
                    path.push(key);
                    copyEntityAttributesToPropertyPathMap(path, (Entity) value, map);
                    path.pop();
                } else {
                    path.push(key);
                    map.put(String.join(".", path), value);
                    path.pop();
                }
            }
        }
    }
}
