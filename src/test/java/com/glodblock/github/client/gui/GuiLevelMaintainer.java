package com.glodblock.github.client.gui;

public class GuiLevelMaintainer {

    @SuppressWarnings("unused")
    private final Widget focusedWidget;

    public GuiLevelMaintainer(boolean focused) {
        this.focusedWidget = new Widget(focused);
    }

    private static final class Widget {

        @SuppressWarnings("unused")
        private final FCGuiTextField textField;

        private Widget(boolean focused) {
            this.textField = new FCGuiTextField(focused);
        }
    }
}
