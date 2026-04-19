package com.github.skystardust.inputmethodblockergtnh.focus;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class InputFocusService {

    private final List<FocusDetector> detectors;
    private Boolean lastState;

    public InputFocusService(List<FocusDetector> detectors) {
        this.detectors = Objects.requireNonNull(detectors);
    }

    public boolean shouldEnableIme(Object screen) {
        return findMatchingDetector(screen) != null;
    }

    public String findMatchingDetector(Object screen) {
        for (FocusDetector detector : detectors) {
            if (detector.hasFocusedTextInput(screen)) {
                return detector.getClass().getSimpleName();
            }
        }
        return null;
    }

    public void update(Object screen, Consumer<Boolean> listener) {
        boolean current = shouldEnableIme(screen);
        if (!current) {
            lastState = Boolean.FALSE;
            listener.accept(false);
            return;
        }

        if (!Objects.equals(lastState, Boolean.TRUE)) {
            lastState = current;
            listener.accept(true);
        }
    }

    public void reset(Consumer<Boolean> listener) {
        lastState = Boolean.FALSE;
        listener.accept(false);
    }
}
