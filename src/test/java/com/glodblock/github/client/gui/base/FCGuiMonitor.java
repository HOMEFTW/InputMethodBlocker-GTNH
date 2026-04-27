package com.glodblock.github.client.gui.base;

import appeng.client.gui.widgets.MEGuiTextField;

public class FCGuiMonitor {

    @SuppressWarnings("unused")
    protected final MEGuiTextField searchField;

    public FCGuiMonitor(boolean focused) {
        this.searchField = new MEGuiTextField(focused);
    }
}
