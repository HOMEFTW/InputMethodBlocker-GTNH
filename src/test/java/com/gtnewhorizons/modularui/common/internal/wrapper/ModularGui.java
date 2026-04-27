package com.gtnewhorizons.modularui.common.internal.wrapper;

public class ModularGui {

    @SuppressWarnings("unused")
    private final ModularUIContext context;

    public ModularGui(boolean focused) {
        this.context = new ModularUIContext(focused);
    }

    public ModularGui(Object focusedWidget) {
        this.context = new ModularUIContext(focusedWidget);
    }
}
