package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiQuartzKnife {

    @SuppressWarnings("unused")
    private final MEGuiTextField textField;

    public GuiQuartzKnife(boolean focused) {
        this.textField = new MEGuiTextField(focused);
    }
}
