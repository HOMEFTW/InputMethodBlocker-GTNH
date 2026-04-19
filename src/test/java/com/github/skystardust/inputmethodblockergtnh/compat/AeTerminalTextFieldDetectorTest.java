package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AeTerminalTextFieldDetectorTest {

    @Test
    void detectsFocusedAe2SearchFieldOnWhitelistedScreen() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(FakeAeMonitorScreen.class.getName(), new String[] { "searchField" });

        Set<String> supportedFieldTypes = new HashSet<>();
        supportedFieldTypes.add(FakeAeTextField.class.getName());

        assertTrue(
            new AeTerminalTextFieldDetector(whitelist, supportedFieldTypes)
                .hasFocusedTextInput(new FakeAeMonitorScreen(true)));
    }

    @Test
    void detectsFocusedAe2ThingsSearchFieldOnWhitelistedScreen() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(FakeAe2ThingsMonitorScreen.class.getName(), new String[] { "searchField" });

        Set<String> supportedFieldTypes = new HashSet<>();
        supportedFieldTypes.add(FakeThTextField.class.getName());

        assertTrue(
            new AeTerminalTextFieldDetector(whitelist, supportedFieldTypes)
                .hasFocusedTextInput(new FakeAe2ThingsMonitorScreen(true)));
    }

    @Test
    void ignoresUnfocusedFieldOnWhitelistedScreen() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(FakeAeMonitorScreen.class.getName(), new String[] { "searchField" });

        Set<String> supportedFieldTypes = new HashSet<>();
        supportedFieldTypes.add(FakeAeTextField.class.getName());

        assertFalse(
            new AeTerminalTextFieldDetector(whitelist, supportedFieldTypes)
                .hasFocusedTextInput(new FakeAeMonitorScreen(false)));
    }

    @Test
    void ignoresFocusedFieldOnNonWhitelistedScreen() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(FakeAeMonitorScreen.class.getName(), new String[] { "searchField" });

        Set<String> supportedFieldTypes = new HashSet<>();
        supportedFieldTypes.add(FakeAeTextField.class.getName());

        assertFalse(
            new AeTerminalTextFieldDetector(whitelist, supportedFieldTypes)
                .hasFocusedTextInput(new FakeOtherScreen(true)));
    }

    private static final class FakeAeMonitorScreen {

        @SuppressWarnings("unused")
        private final FakeAeTextField searchField;

        private FakeAeMonitorScreen(boolean focused) {
            this.searchField = new FakeAeTextField(focused);
        }
    }

    private static final class FakeAe2ThingsMonitorScreen {

        @SuppressWarnings("unused")
        private final FakeThTextField searchField;

        private FakeAe2ThingsMonitorScreen(boolean focused) {
            this.searchField = new FakeThTextField(focused);
        }
    }

    private static final class FakeOtherScreen {

        @SuppressWarnings("unused")
        private final FakeAeTextField searchField;

        private FakeOtherScreen(boolean focused) {
            this.searchField = new FakeAeTextField(focused);
        }
    }

    private static class FakeAeTextField {

        private final boolean focused;

        private FakeAeTextField(boolean focused) {
            this.focused = focused;
        }

        public boolean isFocused() {
            return focused;
        }
    }

    private static final class FakeThTextField extends FakeAeTextField {

        private FakeThTextField(boolean focused) {
            super(focused);
        }
    }
}
