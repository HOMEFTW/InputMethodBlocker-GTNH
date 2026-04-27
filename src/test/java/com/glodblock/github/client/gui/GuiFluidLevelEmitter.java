package com.glodblock.github.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiFluidLevelEmitter {

    @SuppressWarnings("unused")
    private final MEGuiTextField amountTextField;

    public GuiFluidLevelEmitter(boolean focused) {
        this.amountTextField = new MEGuiTextField(focused);
    }
}
