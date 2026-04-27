package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiAmount {

    @SuppressWarnings("unused")
    protected final MEGuiTextField amountTextField;

    public GuiAmount(boolean focused) {
        this.amountTextField = new MEGuiTextField(focused);
    }
}
