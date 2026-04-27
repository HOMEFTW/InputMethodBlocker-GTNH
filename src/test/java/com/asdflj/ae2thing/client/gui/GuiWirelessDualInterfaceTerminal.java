package com.asdflj.ae2thing.client.gui;

import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;

public class GuiWirelessDualInterfaceTerminal extends GuiBaseInterfaceWireless {

    @SuppressWarnings("unused")
    private final ItemPanel itemPanel;

    public GuiWirelessDualInterfaceTerminal(boolean focusedItemPanelSearch) {
        super(false);
        this.itemPanel = new ItemPanel(focusedItemPanelSearch);
    }

    public GuiWirelessDualInterfaceTerminal(
        boolean focusedInputSearch,
        boolean focusedOutputSearch,
        boolean focusedNameSearch,
        boolean focusedItemPanelSearch) {
        super(focusedInputSearch, focusedOutputSearch, focusedNameSearch);
        this.itemPanel = new ItemPanel(focusedItemPanelSearch);
    }

    private static final class ItemPanel {

        @SuppressWarnings("unused")
        private final THGuiTextField searchField;

        private ItemPanel(boolean focused) {
            this.searchField = new THGuiTextField(focused);
        }
    }
}

class GuiBaseInterfaceWireless {

    @SuppressWarnings("unused")
    private final THGuiTextField searchFieldInputs;

    @SuppressWarnings("unused")
    private final THGuiTextField searchFieldOutputs;

    @SuppressWarnings("unused")
    private final THGuiTextField searchFieldNames;

    GuiBaseInterfaceWireless(boolean focusedBaseSearch) {
        this(focusedBaseSearch, false, false);
    }

    GuiBaseInterfaceWireless(boolean focusedInputSearch, boolean focusedOutputSearch, boolean focusedNameSearch) {
        this.searchFieldInputs = new THGuiTextField(focusedInputSearch);
        this.searchFieldOutputs = new THGuiTextField(focusedOutputSearch);
        this.searchFieldNames = new THGuiTextField(focusedNameSearch);
    }
}
