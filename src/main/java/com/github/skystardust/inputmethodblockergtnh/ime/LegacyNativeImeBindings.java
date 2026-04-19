package com.github.skystardust.inputmethodblockergtnh.ime;

import com.github.skystardust.InputMethodBlocker.NativeUtils;

public class LegacyNativeImeBindings implements NativeImeBindings {

    @Override
    public void activeInputMethod(String windowName) {
        NativeUtils.activeInputMethod(windowName);
    }

    @Override
    public void inactiveInputMethod(String windowName) {
        NativeUtils.inactiveInputMethod(windowName);
    }
}
