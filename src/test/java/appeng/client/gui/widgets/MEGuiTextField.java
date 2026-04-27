package appeng.client.gui.widgets;

public class MEGuiTextField {

    private final boolean focused;

    public MEGuiTextField(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
