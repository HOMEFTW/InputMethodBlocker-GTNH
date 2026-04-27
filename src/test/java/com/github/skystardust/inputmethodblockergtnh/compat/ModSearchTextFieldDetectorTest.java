package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ModSearchTextFieldDetectorTest {

    @Test
    void detectsFocusedInstanceFieldWhenAnyWhitelistedFieldIsFocused() {
        Map<String, String[]> instanceWhitelist = new HashMap<>();
        instanceWhitelist.put(FakeAngelicaScreen.class.getName(), new String[] { "searchBox", "testArea" });

        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add(FakeBaseTextField.class.getName());

        Set<String> focusMethods = new HashSet<>();
        focusMethods.add("isFocused");
        focusMethods.add("focused");

        assertTrue(
            new ModSearchTextFieldDetector(instanceWhitelist, new ArrayList<>(), supportedTypes, focusMethods)
                .hasFocusedTextInput(new FakeAngelicaScreen(false, true)));
    }

    @Test
    void detectsFocusedStaticFieldWithLegacyFocusedMethod() {
        Map<String, String[]> instanceWhitelist = new HashMap<>();
        Set<String> supportedTypes = new HashSet<>();
        supportedTypes.add(FakeNeiTextField.class.getName());

        Set<String> focusMethods = new HashSet<>();
        focusMethods.add("focused");

        FakeNeiLayoutManager.searchField = new FakeNeiSearchField(true);

        List<WhitelistedReflectiveTextFieldDetector.StaticFieldBinding> staticBindings = new ArrayList<>();
        staticBindings.add(
            new WhitelistedReflectiveTextFieldDetector.StaticFieldBinding(
                FakeContainerScreen.class.getName(),
                FakeNeiLayoutManager.class.getName(),
                new String[] { "searchField" }));

        assertTrue(
            new ModSearchTextFieldDetector(instanceWhitelist, staticBindings, supportedTypes, focusMethods)
                .hasFocusedTextInput(new FakeContainerScreen()));
    }

    @Test
    void ignoresDisplayOnlyFieldsThatAreNotWhitelisted() {
        assertFalse(new ModSearchTextFieldDetector().hasFocusedTextInput(new FakeInvseeScreen()));
    }

    @Test
    void defaultWhitelistDetectsAngelicaGuiTextFieldsWithObfuscatedFocusMethod() {
        assertTrue(
            new ModSearchTextFieldDetector()
                .hasFocusedTextInput(new com.gtnewhorizons.angelica.client.gui.FontConfigScreen(false, true)));
    }

    @Test
    void defaultWhitelistDetectsNeiOptionTextFields() {
        assertTrue(new ModSearchTextFieldDetector().hasFocusedTextInput(new codechicken.nei.config.GuiOptionList(true)));
    }

    @Test
    void defaultWhitelistDetectsNeiPresetFields() {
        assertTrue(
            new ModSearchTextFieldDetector()
                .hasFocusedTextInput(new codechicken.nei.config.preset.GuiPresetSettings(true, false)));
        assertTrue(
            new ModSearchTextFieldDetector()
                .hasFocusedTextInput(new codechicken.nei.config.preset.GuiPresetSettings(false, true)));
    }

    @Test
    void defaultWhitelistDetectsNeiDebugHandlerIntegerFields() {
        codechicken.nei.recipe.debug.DebugHandlerWidget.instance =
            new codechicken.nei.recipe.debug.DebugHandlerWidget(true);

        assertTrue(
            new ModSearchTextFieldDetector()
                .hasFocusedTextInput(new net.minecraft.client.gui.inventory.GuiContainer()));
    }

    private static final class FakeAngelicaScreen {

        @SuppressWarnings("unused")
        private final FakeGuiTextField searchBox;

        @SuppressWarnings("unused")
        private final FakeGuiTextField testArea;

        private FakeAngelicaScreen(boolean searchFocused, boolean testFocused) {
            this.searchBox = new FakeGuiTextField(searchFocused);
            this.testArea = new FakeGuiTextField(testFocused);
        }
    }

    private static final class FakeContainerScreen {}

    private static final class FakeInvseeScreen {

        @SuppressWarnings("unused")
        private final FakeDisplayTextField textField = new FakeDisplayTextField();
    }

    private static class FakeBaseTextField {

        private final boolean focused;

        private FakeBaseTextField(boolean focused) {
            this.focused = focused;
        }

        public boolean isFocused() {
            return focused;
        }
    }

    private static final class FakeGuiTextField extends FakeBaseTextField {

        private FakeGuiTextField(boolean focused) {
            super(focused);
        }
    }

    private static class FakeNeiTextField {

        private final boolean focused;

        private FakeNeiTextField(boolean focused) {
            this.focused = focused;
        }

        public boolean focused() {
            return focused;
        }
    }

    private static final class FakeNeiSearchField extends FakeNeiTextField {

        private FakeNeiSearchField(boolean focused) {
            super(focused);
        }
    }

    private static final class FakeNeiLayoutManager {

        @SuppressWarnings("unused")
        private static FakeNeiSearchField searchField;
    }

    private static final class FakeDisplayTextField {}
}
