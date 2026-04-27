package com.github.skystardust.inputmethodblockergtnh.compat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AeTerminalTextFieldDetector extends WhitelistedReflectiveTextFieldDetector {

    private static final Set<String> TEXT_FIELD_CLASSES = createTextFieldClasses();
    private static final Map<String, String[]> SCREEN_FIELD_WHITELIST = createScreenFieldWhitelist();
    private static final Set<String> FOCUS_METHOD_NAMES = createFocusMethodNames();

    public AeTerminalTextFieldDetector() {
        this(SCREEN_FIELD_WHITELIST, TEXT_FIELD_CLASSES, FOCUS_METHOD_NAMES);
    }

    AeTerminalTextFieldDetector(Map<String, String[]> screenFieldWhitelist, Set<String> textFieldClasses) {
        this(screenFieldWhitelist, textFieldClasses, FOCUS_METHOD_NAMES);
    }

    AeTerminalTextFieldDetector(
        Map<String, String[]> screenFieldWhitelist,
        Set<String> textFieldClasses,
        Set<String> focusMethodNames) {
        super(textFieldClasses, screenFieldWhitelist, Collections.emptyList(), focusMethodNames);
    }

    private static Set<String> createTextFieldClasses() {
        Set<String> classes = new HashSet<>();
        classes.add("net.minecraft.client.gui.GuiTextField");
        classes.add("appeng.client.gui.widgets.MEGuiTextField");
        classes.add("com.asdflj.ae2thing.client.gui.widget.METextField");
        classes.add("com.asdflj.ae2thing.client.gui.widget.THGuiTextField");
        classes.add("com.glodblock.github.client.gui.FCGuiTextField");
        return Collections.unmodifiableSet(classes);
    }

    private static Set<String> createFocusMethodNames() {
        Set<String> methods = new HashSet<>();
        methods.add("isFocused");
        methods.add("func_146206_l");
        return Collections.unmodifiableSet(methods);
    }

    private static Map<String, String[]> createScreenFieldWhitelist() {
        Map<String, String[]> screens = new HashMap<>();
        screens.put("appeng.client.gui.implementations.GuiAmount", new String[] { "amountTextField" });
        screens.put("appeng.client.gui.implementations.GuiCellRestriction", new String[] { "amountField", "typesField" });
        screens.put("appeng.client.gui.implementations.GuiCraftConfirm", new String[] { "searchField" });
        screens.put("appeng.client.gui.implementations.GuiCraftingCPU", new String[] { "searchField" });
        screens.put("appeng.client.gui.implementations.GuiLevelEmitter", new String[] { "amountTextField" });
        screens.put("appeng.client.gui.implementations.GuiMEMonitorable", new String[] { "searchField" });
        screens.put("appeng.client.gui.implementations.GuiOptimizePatterns", new String[] { "amountToCraft" });
        screens.put("appeng.client.gui.implementations.GuiOreFilter", new String[] { "textField" });
        screens.put("appeng.client.gui.implementations.GuiPatternItemRenamer", new String[] { "textField" });
        screens.put("appeng.client.gui.implementations.GuiQuartzKnife", new String[] { "textField" });
        screens.put("appeng.client.gui.implementations.GuiRenamer", new String[] { "textField" });
        screens.put(
            "appeng.client.gui.implementations.GuiInterfaceTerminal",
            new String[] { "searchFieldInputs", "searchFieldOutputs", "searchFieldNames" });
        screens.put("com.glodblock.github.client.gui.base.FCGuiAmount", new String[] { "amountBox" });
        screens.put("com.glodblock.github.client.gui.base.FCGuiMonitor", new String[] { "searchField" });
        screens.put(
            "com.glodblock.github.client.gui.GuiLevelTerminal",
            new String[] { "searchFieldOutputs", "searchFieldNames" });
        screens.put("com.glodblock.github.client.gui.GuiLevelMaintainer", new String[] { "focusedWidget.textField" });
        screens.put("com.glodblock.github.client.gui.GuiFluidLevelEmitter", new String[] { "amountTextField" });
        screens.put("com.glodblock.github.client.gui.GuiRenamer", new String[] { "textField" });
        screens.put("com.glodblock.github.client.gui.GuiMagnetFilter", new String[] { "oreDict" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiMonitor", new String[] { "searchField" });
        screens.put(
            "com.asdflj.ae2thing.client.gui.GuiBaseInterfaceWireless",
            new String[] { "searchFieldInputs", "searchFieldOutputs", "searchFieldNames" });
        screens.put(
            "com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal",
            new String[] { "searchField", "components.textField", "clickables" });
        screens.put(
            "com.asdflj.ae2thing.client.gui.GuiWirelessDistributor",
            new String[] { "searchField", "components.textField" });
        screens.put(
            "com.asdflj.ae2thing.client.gui.GuiWirelessDualInterfaceTerminal",
            new String[] {
                "searchFieldInputs",
                "searchFieldOutputs",
                "searchFieldNames",
                "itemPanel.searchField",
                "panels.searchField" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiCraftingTerminal", new String[] { "searchField" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiAmount", new String[] { "amountBox" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiPatternValueName", new String[] { "textField" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiRenamer", new String[] { "textField" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiFluidPacketEncoder", new String[] { "level" });
        return Collections.unmodifiableMap(screens);
    }
}
