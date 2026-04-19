package com.gtnewhorizons.modularui.common.widget.textfield;

public class TextFieldWidget {

    private final boolean focused;

    public TextFieldWidget(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
