package com.github.skystardust.inputmethodblockergtnh.focus;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FocusArchitectureGuardTest {

    @Test
    void noLongerShipsGenericReflectionWalker() {
        assertThrows(
            ClassNotFoundException.class,
            () -> Class.forName("com.github.skystardust.inputmethodblockergtnh.focus.ReflectionWalker"));
    }
}
