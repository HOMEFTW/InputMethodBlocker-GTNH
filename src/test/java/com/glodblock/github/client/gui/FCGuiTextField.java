package com.glodblock.github.client.gui;

public class FCGuiTextField {

    private final boolean focused;

    public FCGuiTextField(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
