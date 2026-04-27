package com.asdflj.ae2thing.client.gui;

import net.minecraft.client.gui.GuiTextField;

public class GuiFluidPacketEncoder {

    @SuppressWarnings("unused")
    private final GuiTextField level;

    public GuiFluidPacketEncoder(boolean focused) {
        this.level = new GuiTextField(focused);
    }
}
