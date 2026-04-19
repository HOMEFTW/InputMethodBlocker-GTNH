package com.github.skystardust.inputmethodblockergtnh.focus;

@FunctionalInterface
public interface FocusDetector {

    boolean hasFocusedTextInput(Object screen);
}
