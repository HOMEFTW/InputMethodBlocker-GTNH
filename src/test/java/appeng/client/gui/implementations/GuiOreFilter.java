package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiOreFilter {

    @SuppressWarnings("unused")
    private final MEGuiTextField textField;

    public GuiOreFilter(boolean focused) {
        this.textField = new MEGuiTextField(focused);
    }
}
