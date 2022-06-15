package io.nuvalence.workmanager.service.domain.dynamicschema;

import io.nuvalence.workmanager.service.domain.dynamicschema.attributes.Document;
import io.nuvalence.workmanager.service.domain.dynamicschema.attributes.DocumentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    void convertObjectMapToDocument() {
        Map<Object, Object> documentMap = new HashMap<Object, Object>();
        documentMap.put("documentId", "94c2ca16-dad1-11ec-b1ac-2aaa794f39fc");
        documentMap.put("status", "PENDING");
        documentMap.put("rejectionReason", "NONE");
        documentMap.put("fileName", "virus.bat");

        Document expected = Document.builder()
                .documentId(UUID.fromString("94c2ca16-dad1-11ec-b1ac-2aaa794f39fc"))
                .status(DocumentStatus.PENDING)
                .rejectionReason(null)
                .build();

        Document received = DataConversionSupport.convert(documentMap, Document.class);

        assertEquals(received.getDocumentId(), expected.getDocumentId());
        assertEquals(received.getStatus(), expected.getStatus());
        assertEquals(received.getRejectionReason(), expected.getRejectionReason());
    }

    @Test
    void convertObjectMapToDocument_noDocumentId() {
        Map<Object, Object> documentMap = new HashMap<>();
        documentMap.put("status", "PENDING");

        Document received = DataConversionSupport.convert(documentMap, Document.class);

        assertNull(received);
    }

    @Test
    void convertObjectMapToDocument_invalidStatus() {
        Map<Object, Object> documentMap = new HashMap<Object, Object>();
        documentMap.put("documentId", "94c2ca16-dad1-11ec-b1ac-2aaa794f39fc");
        documentMap.put("status", "Jib-Jub-Scrib-Scrub");
        documentMap.put("fileName", "virus.bat");

        Document received = DataConversionSupport.convert(documentMap, Document.class);

        assertNull(received.getRejectionReason());
        assertEquals(received.getStatus(), DocumentStatus.PENDING);
    }

    @Test
    void convertObjectMapToDocument_nullStatusReason() {
        Map<Object, Object> documentMap = new HashMap<Object, Object>();
        documentMap.put("documentId", "94c2ca16-dad1-11ec-b1ac-2aaa794f39fc");
        documentMap.put("status", DocumentStatus.PENDING.toString());
        documentMap.put("rejectionReason", null);
        documentMap.put("fileName", "virus.bat");

        Document received = DataConversionSupport.convert(documentMap, Document.class);

        assertNull(received.getRejectionReason());
    }

    @Test
    void convertObjectMapToDocument_optionalFields() {
        Map<Object, Object> documentMap = new HashMap<Object, Object>();
        documentMap.put("documentId", "94c2ca16-dad1-11ec-b1ac-2aaa794f39fc");
        documentMap.put("fileName", "koolade-license");

        Document received = DataConversionSupport.convert(documentMap, Document.class);

        assertEquals(UUID.fromString((String) documentMap.get("documentId")), received.getDocumentId());
        assertEquals(DocumentStatus.PENDING, received.getStatus());
        assertNull(received.getRejectionReason());

        documentMap.put("status", DocumentStatus.REJECTED.toString());
        received = DataConversionSupport.convert(documentMap, Document.class);

        assertEquals(received.getStatus(), DocumentStatus.REJECTED);
    }

    @Test
    void throwsUnsupportedOperationExceptionIfNoConverterFound() {
        assertThrows(
            UnsupportedOperationException.class,
            () -> DataConversionSupport.convert("", DataConversionSupportTest.class)
        );
    }
}
