package io.nuvalence.workmanager.service.domain.dynamicschema.attributes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Represents a custom dynamic schema attribute for a Document.
 */
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class Document {

    private final UUID documentId;
    private DocumentStatus status;
    private DocumentRejectionReason rejectionReason;
    private String fileName;
    private String fileType;
    private String questionKey;

}
