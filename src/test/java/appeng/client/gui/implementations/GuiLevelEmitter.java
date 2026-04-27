package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiLevelEmitter {

    @SuppressWarnings("unused")
    private final MEGuiTextField amountTextField;

    public GuiLevelEmitter(boolean focused) {
        this.amountTextField = new MEGuiTextField(focused);
    }
}
