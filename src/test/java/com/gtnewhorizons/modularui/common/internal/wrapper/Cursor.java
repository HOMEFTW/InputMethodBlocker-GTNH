package com.gtnewhorizons.modularui.common.internal.wrapper;

final class Cursor {

    @SuppressWarnings("unused")
    private final Object focused;

    Cursor(boolean focused) {
        this(new com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget(focused));
    }

    Cursor(Object focused) {
        this.focused = focused;
    }
}
