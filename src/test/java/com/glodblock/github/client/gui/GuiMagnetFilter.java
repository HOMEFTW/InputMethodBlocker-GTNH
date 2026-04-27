package com.glodblock.github.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiMagnetFilter {

    @SuppressWarnings("unused")
    protected final MEGuiTextField oreDict;

    public GuiMagnetFilter(boolean focused) {
        this.oreDict = new MEGuiTextField(focused);
    }
}
