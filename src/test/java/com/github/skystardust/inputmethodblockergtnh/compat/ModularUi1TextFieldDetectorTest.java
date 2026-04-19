package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ModularUi1TextFieldDetectorTest {

    @Test
    void detectsFocusedMui1TextFieldOnWhitelistedScreenField() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(WhitelistedMui1Screen.class.getName(), new String[] { "textField" });

        assertTrue(new ModularUi1TextFieldDetector(whitelist).hasFocusedTextInput(new WhitelistedMui1Screen(true)));
    }

    @Test
    void ignoresUnfocusedMui1TextFieldOnWhitelistedScreenField() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(WhitelistedMui1Screen.class.getName(), new String[] { "textField" });

        assertFalse(new ModularUi1TextFieldDetector(whitelist).hasFocusedTextInput(new WhitelistedMui1Screen(false)));
    }

    @Test
    void ignoresFocusedMui1TextFieldOnNonWhitelistedScreen() {
        Map<String, String[]> whitelist = new HashMap<>();
        whitelist.put(WhitelistedMui1Screen.class.getName(), new String[] { "textField" });

        assertFalse(new ModularUi1TextFieldDetector(whitelist).hasFocusedTextInput(new NonWhitelistedMui1Screen(true)));
    }

    @Test
    void defaultWhitelistStartsEmpty() {
        assertFalse(new ModularUi1TextFieldDetector().hasFocusedTextInput(new WhitelistedMui1Screen(true)));
    }

    private static final class WhitelistedMui1Screen {

        @SuppressWarnings("unused")
        private final com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget textField;

        private WhitelistedMui1Screen(boolean focused) {
            this.textField = new com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget(focused);
        }
    }

    private static final class NonWhitelistedMui1Screen {

        @SuppressWarnings("unused")
        private final com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget textField;

        private NonWhitelistedMui1Screen(boolean focused) {
            this.textField = new com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget(focused);
        }
    }
}
