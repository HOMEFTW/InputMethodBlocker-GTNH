package com.github.skystardust.inputmethodblockergtnh.focus;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class ConditionalFocusDetector implements FocusDetector {

    private final BooleanSupplier enabled;
    private final FocusDetector delegate;

    public ConditionalFocusDetector(BooleanSupplier enabled, FocusDetector delegate) {
        this.enabled = Objects.requireNonNull(enabled);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean hasFocusedTextInput(Object screen) {
        return enabled.getAsBoolean() && delegate.hasFocusedTextInput(screen);
    }
}
