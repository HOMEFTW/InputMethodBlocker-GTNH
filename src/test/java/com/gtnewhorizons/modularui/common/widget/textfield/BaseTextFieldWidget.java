package com.gtnewhorizons.modularui.common.widget.textfield;

public class BaseTextFieldWidget {

    private final boolean focused;

    public BaseTextFieldWidget(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
