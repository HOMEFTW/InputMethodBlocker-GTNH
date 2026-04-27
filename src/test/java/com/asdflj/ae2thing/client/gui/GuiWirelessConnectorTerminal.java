package com.asdflj.ae2thing.client.gui;

import java.util.Collections;
import java.util.List;

import com.asdflj.ae2thing.client.gui.widget.METextField;
import com.asdflj.ae2thing.client.gui.widget.THGuiTextField;

public class GuiWirelessConnectorTerminal {

    @SuppressWarnings("unused")
    private final THGuiTextField searchField;

    @SuppressWarnings("unused")
    private final List<Component> components;

    @SuppressWarnings("unused")
    private final List<Object> clickables;

    public GuiWirelessConnectorTerminal(boolean focusedComponentName) {
        this.searchField = new THGuiTextField(false);
        this.components = Collections.singletonList(new Component(focusedComponentName));
        this.clickables = Collections.singletonList(new METextField(false));
    }

    public GuiWirelessConnectorTerminal(boolean focusedSearch, boolean focusedComponentName, boolean focusedClickable) {
        this.searchField = new THGuiTextField(focusedSearch);
        this.components = Collections.singletonList(new Component(focusedComponentName));
        this.clickables = Collections.singletonList(new METextField(focusedClickable));
    }

    private static final class Component {

        @SuppressWarnings("unused")
        private final METextField textField;

        private Component(boolean focused) {
            this.textField = new METextField(focused);
        }
    }
}
