package io.nuvalence.workmanager.service.utils.jpa;

import org.springframework.util.StringUtils;

import java.util.UUID;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA converter to map UUID class to db column.
 */
@Converter(autoApply = true)
public class UuidPersistenceConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID entityValue) {
        return (entityValue == null) ? null : entityValue.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String databaseValue) {
        return StringUtils.hasLength(databaseValue)
                ? UUID.fromString(databaseValue.trim())
                : null; // NOPMD
    }
}
