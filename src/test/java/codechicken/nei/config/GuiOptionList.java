package codechicken.nei.config;

import java.util.Collections;
import java.util.List;

public class GuiOptionList {

    @SuppressWarnings("unused")
    private final OptionScrollSlot slot;

    public GuiOptionList(boolean focused) {
        this.slot = new OptionScrollSlot(focused);
    }

    private static final class OptionScrollSlot {

        @SuppressWarnings("unused")
        private final List<OptionTextField> options;

        private OptionScrollSlot(boolean focused) {
            this.options = Collections.singletonList(new OptionTextField(focused));
        }
    }
}
