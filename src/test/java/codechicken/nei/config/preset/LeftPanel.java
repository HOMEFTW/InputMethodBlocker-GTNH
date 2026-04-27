package codechicken.nei.config.preset;

final class LeftPanel {

    @SuppressWarnings("unused")
    private final codechicken.nei.TextField nameField;

    LeftPanel(boolean focused) {
        this.nameField = new codechicken.nei.TextField(focused);
    }
}
