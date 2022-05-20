package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import lombok.experimental.Delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Provides validation process context to validation constrains and collects resulting violations.
 */
public class ValidationContext implements List<ConstraintViolation> {
    @Delegate
    private final List<ConstraintViolation> violations = new LinkedList<>();

    private final Stack<String> path = new Stack<>();

    public String getPath() {
        return String.join(".", path);
    }

    public void pushPath(final String pathPart) {
        path.push(pathPart);
    }

    public void popPath() {
        path.pop();
    }
}
