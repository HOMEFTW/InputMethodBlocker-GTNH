package com.asdflj.ae2thing.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiPatternValueName {

    @SuppressWarnings("unused")
    private final MEGuiTextField textField;

    public GuiPatternValueName(boolean focused) {
        this.textField = new MEGuiTextField(focused);
    }
}
