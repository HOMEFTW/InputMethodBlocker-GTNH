package codechicken.nei.recipe.debug;

import java.util.Collections;
import java.util.List;

public class DebugHandlerWidget {

    public static DebugHandlerWidget instance;

    @SuppressWarnings("unused")
    private final WidgetContainer container;

    public DebugHandlerWidget(boolean focused) {
        this.container = new WidgetContainer(focused);
    }

    private static final class WidgetContainer {

        @SuppressWarnings("unused")
        private final List<IntegerField> widgets;

        private WidgetContainer(boolean focused) {
            this.widgets = Collections.singletonList(new IntegerField(focused));
        }
    }
}
