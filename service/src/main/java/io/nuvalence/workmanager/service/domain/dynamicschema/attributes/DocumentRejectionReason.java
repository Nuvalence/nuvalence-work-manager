package io.nuvalence.workmanager.service.domain.dynamicschema.attributes;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the reason that a status may occur.
 */
public enum DocumentRejectionReason {
    BLURRY_DOCUMENT("Blurry-document"); // example reason, none provided yet.

    @JsonValue
    private final String text;

    DocumentRejectionReason(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     * Validation for Document rejection reason enum.
     * @param text Incoming text from json.
     * @return A valid Document Rejection Reason.
     */
    public static DocumentRejectionReason fromText(String text) {
        for (DocumentRejectionReason reason: DocumentRejectionReason.values()) {
            if (reason.toString().equalsIgnoreCase(text)) {
                return reason;
            }
        }
        return null;
    }
}
