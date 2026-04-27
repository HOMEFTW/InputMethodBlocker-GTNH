package com.glodblock.github.client.gui.base;

import com.glodblock.github.client.gui.FCGuiTextField;

public class FCGuiAmount {

    @SuppressWarnings("unused")
    protected final FCGuiTextField amountBox;

    public FCGuiAmount(boolean focused) {
        this.amountBox = new FCGuiTextField(focused);
    }
}
