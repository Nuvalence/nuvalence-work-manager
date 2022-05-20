package io.nuvalence.workmanager.service.domain.dynamicschema;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DataConversionSupportTest {

    @Test
    void convertStringToLocalDate() {
        assertEquals(LocalDate.of(2022, 4, 12), DataConversionSupport.convert("2022-04-12", LocalDate.class));
        assertNull(DataConversionSupport.convert("", LocalDate.class));
    }

    @Test
    void  convertStringToLocalTime() {
        assertEquals(LocalTime.of(12, 30, 0), DataConversionSupport.convert("12:30:00", LocalTime.class));
        assertNull(DataConversionSupport.convert("", LocalTime.class));
    }

    @Test
    void  convertStringToInteger() {
        assertEquals(5, DataConversionSupport.convert("5", Integer.class));
        assertNull(DataConversionSupport.convert("", Integer.class));
    }

    @Test
    void  convertStringToBigDecimal() {
        assertEquals(BigDecimal.valueOf(5), DataConversionSupport.convert("5", BigDecimal.class));
        assertNull(DataConversionSupport.convert("", BigDecimal.class));
    }

    @Test
    void  convertIntegerToBigDecimal() {
        assertEquals(new BigDecimal("5"), DataConversionSupport.convert(5, BigDecimal.class));
    }

    @Test
    void  convertDoubleToBigDecimal() {
        assertEquals(new BigDecimal("5.99"), DataConversionSupport.convert(5.99, BigDecimal.class));
    }

    @Test
    void  convertStringToBoolean() {
        assertTrue(DataConversionSupport.convert("yes", Boolean.class));
        assertTrue(DataConversionSupport.convert("Yes", Boolean.class));
        assertTrue(DataConversionSupport.convert("YES", Boolean.class));
        assertTrue(DataConversionSupport.convert("true", Boolean.class));
        assertTrue(DataConversionSupport.convert("True", Boolean.class));
        assertTrue(DataConversionSupport.convert("TRUE", Boolean.class));
        assertFalse(DataConversionSupport.convert("no", Boolean.class));
        assertFalse(DataConversionSupport.convert("No", Boolean.class));
        assertFalse(DataConversionSupport.convert("NO", Boolean.class));
        assertFalse(DataConversionSupport.convert("false", Boolean.class));
        assertFalse(DataConversionSupport.convert("False", Boolean.class));
        assertFalse(DataConversionSupport.convert("FALSE", Boolean.class));
        assertNull(DataConversionSupport.convert("Foo", Boolean.class));
        assertNull(DataConversionSupport.convert("", Boolean.class));
    }

    @Test
    void throwsUnsupportedOperationExceptionIfNoConverterFound() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> DataConversionSupport.convert("", DataConversionSupportTest.class)
        );
    }
}