package codechicken.nei.config.preset;

final class RightPanel {

    @SuppressWarnings("unused")
    private final codechicken.nei.TextField searchField;

    RightPanel(boolean focused) {
        this.searchField = new codechicken.nei.TextField(focused);
    }
}
