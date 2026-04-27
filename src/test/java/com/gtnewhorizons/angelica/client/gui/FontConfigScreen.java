package com.gtnewhorizons.angelica.client.gui;

import net.minecraft.client.gui.GuiTextField;

public class FontConfigScreen {

    @SuppressWarnings("unused")
    private final GuiTextField searchBox;

    @SuppressWarnings("unused")
    private final GuiTextField testArea;

    public FontConfigScreen(boolean searchFocused, boolean testAreaFocused) {
        this.searchBox = new GuiTextField(searchFocused);
        this.testArea = new GuiTextField(testAreaFocused);
    }
}
