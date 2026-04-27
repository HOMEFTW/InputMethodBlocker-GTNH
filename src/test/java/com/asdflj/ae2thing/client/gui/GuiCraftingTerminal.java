package com.asdflj.ae2thing.client.gui;

import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;

public class GuiCraftingTerminal extends GuiMonitor {

    public GuiCraftingTerminal(boolean focused) {
        super(focused);
    }
}

class GuiMonitor {

    @SuppressWarnings("unused")
    private final THGuiTextField searchField;

    GuiMonitor(boolean focused) {
        this.searchField = new THGuiTextField(focused);
    }
}
