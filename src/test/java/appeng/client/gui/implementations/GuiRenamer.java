package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiRenamer {

    @SuppressWarnings("unused")
    private final MEGuiTextField textField;

    public GuiRenamer(boolean focused) {
        this.textField = new MEGuiTextField(focused);
    }
}
