package com.glodblock.github.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiRenamer {

    @SuppressWarnings("unused")
    protected final MEGuiTextField textField;

    public GuiRenamer(boolean focused) {
        this.textField = new MEGuiTextField(focused);
    }
}
