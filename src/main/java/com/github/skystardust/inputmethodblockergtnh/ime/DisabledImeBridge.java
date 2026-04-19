package com.github.skystardust.inputmethodblockergtnh.ime;

public class DisabledImeBridge implements ImeBridge {

    @Override
    public void initialize() {}

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void setImeActive(boolean active) {}
}
