package io.nuvalence.workmanager.service.utils.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import javax.persistence.AttributeConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UuidPersistenceConverterTest {
    private String uuidString;
    private UUID uuid;
    private AttributeConverter<UUID, String> converter;

    @BeforeEach
    void setup() {
        uuidString = "a8cf36ed-28b6-4ce4-9c52-48b4f33e122a";
        uuid = UUID.fromString(uuidString);
        converter = new UuidPersistenceConverter();
    }

    @Test
    void convertToDatabaseColumn() {
        assertEquals(uuidString, converter.convertToDatabaseColumn(uuid));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute() {
        assertEquals(uuid, converter.convertToEntityAttribute(uuidString));
        assertNull(converter.convertToEntityAttribute(null));
    }
}