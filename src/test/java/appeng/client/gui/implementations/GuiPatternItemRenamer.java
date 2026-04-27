package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiPatternItemRenamer {

    @SuppressWarnings("unused")
    private final MEGuiTextField textField;

    public GuiPatternItemRenamer(boolean focused) {
        this.textField = new MEGuiTextField(focused);
    }
}
