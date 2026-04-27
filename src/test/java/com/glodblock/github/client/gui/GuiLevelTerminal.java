package com.glodblock.github.client.gui;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiLevelTerminal {

    @SuppressWarnings("unused")
    private final MEGuiTextField searchFieldOutputs;

    @SuppressWarnings("unused")
    private final MEGuiTextField searchFieldNames;

    public GuiLevelTerminal(boolean outputFocused, boolean nameFocused) {
        this.searchFieldOutputs = new MEGuiTextField(outputFocused);
        this.searchFieldNames = new MEGuiTextField(nameFocused);
    }
}
