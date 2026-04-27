package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiCraftingCPU {

    @SuppressWarnings("unused")
    private final MEGuiTextField searchField;

    public GuiCraftingCPU(boolean focused) {
        this.searchField = new MEGuiTextField(focused);
    }
}
