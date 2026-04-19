package com.github.skystardust.inputmethodblockergtnh.focus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class InputFocusServiceTest {

    @Test
    void reportsFocusedWhenAnyDetectorMatches() {
        InputFocusService service = new InputFocusService(
            Arrays.asList(screen -> false, screen -> true, screen -> false));

        assertTrue(service.shouldEnableIme(new Object()));
    }

    @Test
    void reportsNotFocusedWhenNoDetectorMatches() {
        InputFocusService service = new InputFocusService(Arrays.asList(screen -> false, screen -> false));

        assertFalse(service.shouldEnableIme(new Object()));
    }

    @Test
    void republishesInactiveDuringResetsWhileDebouncingActiveTransitions() {
        AtomicInteger changes = new AtomicInteger();
        InputFocusService service = new InputFocusService(Arrays.asList(screen -> Boolean.TRUE.equals(screen)));

        service.update(Boolean.TRUE, active -> changes.incrementAndGet());
        service.update(Boolean.TRUE, active -> changes.incrementAndGet());
        service.update(Boolean.FALSE, active -> changes.incrementAndGet());
        service.reset(active -> changes.incrementAndGet());

        assertEquals(3, changes.get());
    }

    @Test
    void republishesInactiveWhileNoDetectorMatches() {
        AtomicInteger changes = new AtomicInteger();
        InputFocusService service = new InputFocusService(Arrays.asList(screen -> false));

        service.update(new Object(), active -> changes.incrementAndGet());
        service.update(new Object(), active -> changes.incrementAndGet());

        assertEquals(2, changes.get());
    }
}
