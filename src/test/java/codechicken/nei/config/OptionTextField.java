package codechicken.nei.config;

public class OptionTextField {

    @SuppressWarnings("unused")
    private final codechicken.nei.TextField textField;

    public OptionTextField(boolean focused) {
        this.textField = new codechicken.nei.TextField(focused);
    }
}
