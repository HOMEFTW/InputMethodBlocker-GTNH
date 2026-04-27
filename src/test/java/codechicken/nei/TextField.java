package codechicken.nei;

public class TextField {

    private final boolean focused;

    public TextField(boolean focused) {
        this.focused = focused;
    }

    public boolean focused() {
        return focused;
    }
}
