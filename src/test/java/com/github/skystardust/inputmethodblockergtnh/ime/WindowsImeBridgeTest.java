package com.github.skystardust.inputmethodblockergtnh.ime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

class WindowsImeBridgeTest {

    @Test
    void becomesAvailableAfterSuccessfulLoadAndDebouncesDuplicateTrueState() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows64(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);
        bridge.setImeActive(true);
        bridge.setImeActive(false);

        assertTrue(bridge.isAvailable());
        assertEquals(1, loader.loads);
        assertEquals(1, bindings.activeCalls);
        assertEquals(2, bindings.inactiveCalls);
    }

    @Test
    void staysDisabledOnWindows32BitRuntime() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows32(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);

        assertFalse(bridge.isAvailable());
        assertEquals(0, loader.loads);
        assertEquals(0, bindings.activeCalls);
        assertEquals(0, bindings.inactiveCalls);
    }

    @Test
    void initializesNativeImeStateAsInactiveAfterSuccessfulLoad() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows64(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();

        assertTrue(bridge.isAvailable());
        assertEquals(1, loader.loads);
        assertEquals(1, bindings.inactiveCalls);
        assertEquals(0, bindings.activeCalls);
    }

    @Test
    void reappliesInactiveEvenWhenAlreadyInactive() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows64(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(false);
        bridge.setImeActive(false);

        assertEquals(3, bindings.inactiveCalls);
    }

    @Test
    void staysDisabledOutsideWindows() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.nonWindows("Linux", "amd64"),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);

        assertFalse(bridge.isAvailable());
        assertEquals(0, loader.loads);
        assertEquals(0, bindings.activeCalls);
    }

    @Test
    void disablesItselfWhenNativeCallThrows() {
        ThrowingBindings bindings = new ThrowingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows64(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);
        bridge.setImeActive(false);

        assertFalse(bridge.isAvailable());
        assertEquals(2, bindings.calls);
    }

    private static final class RecordingLoader implements NativeLibraryLoader {

        private int loads;

        @Override
        public void load(File libraryFile) {
            loads++;
        }
    }

    private static final class RecordingBindings implements NativeImeBindings {

        private int activeCalls;
        private int inactiveCalls;

        @Override
        public void activeInputMethod(String windowName) {
            activeCalls++;
        }

        @Override
        public void inactiveInputMethod(String windowName) {
            inactiveCalls++;
        }
    }

    private static final class ThrowingBindings implements NativeImeBindings {

        private int calls;

        @Override
        public void activeInputMethod(String windowName) {
            calls++;
            throw new IllegalStateException("boom");
        }

        @Override
        public void inactiveInputMethod(String windowName) {
            calls++;
        }
    }
}
