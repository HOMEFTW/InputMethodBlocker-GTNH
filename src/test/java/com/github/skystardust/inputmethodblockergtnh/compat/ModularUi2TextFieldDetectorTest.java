package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ModularUi2TextFieldDetectorTest {

    @Test
    void detectsFocusedMui2TextFieldOnWhitelistedScreenField() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(WhitelistedMui2Screen.class.getName(), new String[] { "textField" });

        assertTrue(new ModularUi2TextFieldDetector(whitelist).hasFocusedTextInput(new WhitelistedMui2Screen(true)));
    }

    @Test
    void ignoresUnfocusedMui2TextFieldOnWhitelistedScreenField() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(WhitelistedMui2Screen.class.getName(), new String[] { "textField" });

        assertFalse(new ModularUi2TextFieldDetector(whitelist).hasFocusedTextInput(new WhitelistedMui2Screen(false)));
    }

    @Test
    void ignoresFocusedMui2TextFieldOnNonWhitelistedScreen() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(WhitelistedMui2Screen.class.getName(), new String[] { "textField" });

        assertFalse(new ModularUi2TextFieldDetector(whitelist).hasFocusedTextInput(new NonWhitelistedMui2Screen(true)));
    }

    @Test
    void defaultWhitelistStartsEmpty() {
        assertFalse(new ModularUi2TextFieldDetector().hasFocusedTextInput(new WhitelistedMui2Screen(true)));
    }

    private static final class WhitelistedMui2Screen {

        @SuppressWarnings("unused")
        private final com.cleanroommc.modularui.widgets.textfield.TextFieldWidget textField;

        private WhitelistedMui2Screen(boolean focused) {
            this.textField = new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget(focused);
        }
    }

    private static final class NonWhitelistedMui2Screen {

        @SuppressWarnings("unused")
        private final com.cleanroommc.modularui.widgets.textfield.TextFieldWidget textField;

        private NonWhitelistedMui2Screen(boolean focused) {
            this.textField = new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget(focused);
        }
    }
}
