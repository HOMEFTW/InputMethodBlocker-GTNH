package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class VanillaTextFieldDetectorTest {

    @Test
    void enablesImeForAlwaysEnabledVanillaScreens() {
        Set<String> alwaysEnabled = new HashSet<>();
        alwaysEnabled.add(FakeGuiChat.class.getName());
        VanillaTextFieldDetector detector = new VanillaTextFieldDetector(
            alwaysEnabled,
            Collections.emptyMap());

        assertTrue(detector.hasFocusedTextInput(new FakeGuiChat()));
    }

    @Test
    void enablesImeForFocusedWhitelistedTextFields() {
        Map<String, String[]> focusedScreens = new HashMap<>();
        focusedScreens.put(FakeGuiRepair.class.getName(), new String[] { "textField" });
        VanillaTextFieldDetector detector = new VanillaTextFieldDetector(
            Collections.emptySet(),
            focusedScreens);

        assertTrue(detector.hasFocusedTextInput(new FakeGuiRepair(true)));
    }

    @Test
    void ignoresUnfocusedWhitelistedTextFields() {
        Map<String, String[]> focusedScreens = new HashMap<>();
        focusedScreens.put(FakeGuiRepair.class.getName(), new String[] { "textField" });
        VanillaTextFieldDetector detector = new VanillaTextFieldDetector(
            Collections.emptySet(),
            focusedScreens);

        assertFalse(detector.hasFocusedTextInput(new FakeGuiRepair(false)));
    }

    @Test
    void ignoresUnsupportedScreensEvenWhenTheyContainFocusedFields() {
        Map<String, String[]> focusedScreens = new HashMap<>();
        focusedScreens.put(FakeGuiRepair.class.getName(), new String[] { "textField" });
        VanillaTextFieldDetector detector = new VanillaTextFieldDetector(
            Collections.emptySet(),
            focusedScreens);

        assertFalse(detector.hasFocusedTextInput(new UnsupportedScreen(true)));
    }

    private static final class FakeGuiChat {}

    private static final class FakeGuiRepair {

        @SuppressWarnings("unused")
        private final FakeTextField textField;

        private FakeGuiRepair(boolean focused) {
            this.textField = new FakeTextField(focused);
        }
    }

    private static final class UnsupportedScreen {

        @SuppressWarnings("unused")
        private final FakeTextField textField;

        private UnsupportedScreen(boolean focused) {
            this.textField = new FakeTextField(focused);
        }
    }

    private static final class FakeTextField {

        private final boolean focused;

        private FakeTextField(boolean focused) {
            this.focused = focused;
        }

        public boolean isFocused() {
            return focused;
        }
    }
}
