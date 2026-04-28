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
    void defaultWhitelistIgnoresAe2ThingsCraftAmountField() {
        assertFalse(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiCraftAmount(true)));
    }

    @Test
    void defaultWhitelistIgnoresAe2CraftAmountField() {
        assertFalse(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiCraftAmount(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2CellRestrictionFields() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiCellRestriction(true, false)));
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiCellRestriction(false, true)));
    }

    @Test
    void defaultWhitelistDetectsAe2SingleTextFieldDialogs() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiOreFilter(true)));
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiRenamer(true)));
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiQuartzKnife(true)));
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiPatternItemRenamer(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2LevelEmitterAndOptimizePatternAmountFields() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiLevelEmitter(true)));
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new appeng.client.gui.implementations.GuiOptimizePatterns(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsInheritedCraftingStatusSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiCraftingStatus(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsInheritedCraftConfirmSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiCraftConfirm(true)));
    }

    @Test
    void defaultWhitelistIgnoresAe2FluidCraftAmountField() {
        assertFalse(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiFluidCraftAmount(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2FluidCraftMonitorSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiFluidMonitor(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2FluidCraftLevelTerminalSearchFields() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiLevelTerminal(true, false)));
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiLevelTerminal(false, true)));
    }

    @Test
    void defaultWhitelistDetectsAe2FluidCraftLevelMaintainerFocusedWidget() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiLevelMaintainer(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2FluidCraftFluidLevelEmitterAmountField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiFluidLevelEmitter(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2FluidCraftRenamerField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiRenamer(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2FluidCraftMagnetFilterField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.glodblock.github.client.gui.GuiMagnetFilter(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsPatternNameField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiPatternValueName(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsFluidPacketLevelField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiFluidPacketEncoder(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsNestedItemPanelSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsComponentNameFieldInList() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal(true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsWirelessConnectorClickableNameField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(
                    new com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal(false, false, true)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsWirelessConnectorSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(
                    new com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal(true, false, false)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsWirelessDualInterfaceNameSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(
                    new com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal(false, false, true, false)));
    }

    @Test
    void defaultWhitelistDetectsAe2ThingsBackpackTerminalSearchField() {
        assertTrue(
            new AeTerminalTextFieldDetector()
                .hasFocusedTextInput(new com.asdflj.ae2thing.client.gui.GuiCraftingTerminal(true)));
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
