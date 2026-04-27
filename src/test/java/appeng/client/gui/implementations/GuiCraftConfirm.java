package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiCraftConfirm {

    @SuppressWarnings("unused")
    private final MEGuiTextField searchField;

    public GuiCraftConfirm(boolean focused) {
        this.searchField = new MEGuiTextField(focused);
    }
}
