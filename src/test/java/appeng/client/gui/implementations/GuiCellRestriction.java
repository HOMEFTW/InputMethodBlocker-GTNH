package appeng.client.gui.implementations;

import appeng.client.gui.widgets.MEGuiTextField;

public class GuiCellRestriction {

    @SuppressWarnings("unused")
    private final MEGuiTextField amountField;

    @SuppressWarnings("unused")
    private final MEGuiTextField typesField;

    public GuiCellRestriction(boolean amountFocused, boolean typesFocused) {
        this.amountField = new MEGuiTextField(amountFocused);
        this.typesField = new MEGuiTextField(typesFocused);
    }
}
