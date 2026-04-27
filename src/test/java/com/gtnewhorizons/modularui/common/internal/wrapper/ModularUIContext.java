package com.gtnewhorizons.modularui.common.internal.wrapper;

final class ModularUIContext {

    @SuppressWarnings("unused")
    private final Cursor cursor;

    ModularUIContext(boolean focused) {
        this(new Cursor(focused));
    }

    ModularUIContext(Object focused) {
        this(new Cursor(focused));
    }

    private ModularUIContext(Cursor cursor) {
        this.cursor = cursor;
    }
}
