package com.github.skystardust.inputmethodblockergtnh.ime;

public interface ImeBridge {

    void initialize();

    boolean isAvailable();

    void setImeActive(boolean active);
}
