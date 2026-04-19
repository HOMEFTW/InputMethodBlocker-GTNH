package com.github.skystardust.inputmethodblockergtnh.focus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class ConditionalFocusDetectorTest {

    @Test
    void skipsDelegateWhenConditionIsFalse() {
        AtomicInteger invocations = new AtomicInteger();
        FocusDetector detector = new ConditionalFocusDetector(
            () -> false,
            screen -> {
                invocations.incrementAndGet();
                return true;
            });

        assertFalse(detector.hasFocusedTextInput(new Object()));
        assertFalse(detector.hasFocusedTextInput(new Object()));
        assertTrue(invocations.get() == 0);
    }

    @Test
    void delegatesWhenConditionIsTrue() {
        AtomicInteger invocations = new AtomicInteger();
        FocusDetector detector = new ConditionalFocusDetector(
            () -> true,
            screen -> {
                invocations.incrementAndGet();
                return true;
            });

        assertTrue(detector.hasFocusedTextInput(new Object()));
        assertTrue(invocations.get() == 1);
    }
}
