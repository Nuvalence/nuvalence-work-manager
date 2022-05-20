package io.nuvalence.workmanager.service.domain.dynamicschema.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationContextTest {
    @Test
    void canTrackCurrentPath() {
        final ValidationContext context = new ValidationContext();

        context.pushPath("root");
        assertEquals("root", context.getPath());

        context.pushPath("child");
        assertEquals("root.child", context.getPath());

        context.popPath();
        assertEquals("root", context.getPath());
    }
}