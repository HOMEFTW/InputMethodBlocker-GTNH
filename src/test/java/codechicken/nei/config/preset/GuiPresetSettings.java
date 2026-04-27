package codechicken.nei.config.preset;

public class GuiPresetSettings {

    @SuppressWarnings("unused")
    private final LeftPanel leftPanel;

    @SuppressWarnings("unused")
    private final RightPanel rightPanel;

    public GuiPresetSettings(boolean nameFocused, boolean searchFocused) {
        this.leftPanel = new LeftPanel(nameFocused);
        this.rightPanel = new RightPanel(searchFocused);
    }
}
