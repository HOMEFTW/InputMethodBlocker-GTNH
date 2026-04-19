package com.github.skystardust.inputmethodblockergtnh.compat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AeTerminalTextFieldDetector extends WhitelistedReflectiveTextFieldDetector {

    private static final Set<String> TEXT_FIELD_CLASSES = createTextFieldClasses();
    private static final Map<String, String[]> SCREEN_FIELD_WHITELIST = createScreenFieldWhitelist();

    public AeTerminalTextFieldDetector() {
        this(SCREEN_FIELD_WHITELIST, TEXT_FIELD_CLASSES);
    }

    AeTerminalTextFieldDetector(Map<String, String[]> screenFieldWhitelist, Set<String> textFieldClasses) {
        super(textFieldClasses, screenFieldWhitelist);
    }

    private static Set<String> createTextFieldClasses() {
        Set<String> classes = new HashSet<>();
        classes.add("appeng.client.gui.widgets.MEGuiTextField");
        classes.add("com.asdflj.ae2thing.client.gui.widget.THGuiTextField");
        return Collections.unmodifiableSet(classes);
    }

    private static Map<String, String[]> createScreenFieldWhitelist() {
        Map<String, String[]> screens = new HashMap<>();
        screens.put("appeng.client.gui.implementations.GuiMEMonitorable", new String[] { "searchField" });
        screens.put(
            "appeng.client.gui.implementations.GuiInterfaceTerminal",
            new String[] { "searchFieldInputs", "searchFieldOutputs", "searchFieldNames" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiMonitor", new String[] { "searchField" });
        screens.put(
            "com.asdflj.ae2thing.client.gui.GuiBaseInterfaceWireless",
            new String[] { "searchFieldInputs", "searchFieldOutputs", "searchFieldNames" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal", new String[] { "searchField" });
        screens.put("com.asdflj.ae2thing.client.gui.GuiWirelessDistributor", new String[] { "searchField" });
        return Collections.unmodifiableMap(screens);
    }
}
