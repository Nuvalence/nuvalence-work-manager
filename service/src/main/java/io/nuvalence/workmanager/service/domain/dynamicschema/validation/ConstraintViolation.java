package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a single validation constraint violation.
 */
@Value
@Builder
public class ConstraintViolation {
    String path;
    String messageTemplate;
    Object value;
    @Singular
    List<Object> args;

    /**
     * Returns message giving details of this violation.
     *
     * @return message as String with contextual values interpolated
     */
    public String getMessage() {
        return MessageFormat.format(
                messageTemplate,
                Stream.concat(
                        Stream.of(path, value),
                        args.stream()
                ).toArray()
        );
    }
}
