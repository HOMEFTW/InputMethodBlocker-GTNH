package com.asdflj.ae2thing.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;

class GuiAmount {

    @SuppressWarnings("unused")
    private final MEGuiTextField amountBox;

    GuiAmount(boolean focused) {
        this.amountBox = new MEGuiTextField(focused);
    }
}

public class GuiCraftAmount extends GuiAmount {

    public GuiCraftAmount(boolean focused) {
        super(focused);
    }
}
