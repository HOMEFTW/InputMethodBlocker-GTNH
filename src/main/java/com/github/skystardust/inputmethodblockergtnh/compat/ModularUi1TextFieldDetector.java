package com.github.skystardust.inputmethodblockergtnh.compat;

import java.util.Collections;
import java.util.Map;

public class ModularUi1TextFieldDetector extends WhitelistedReflectiveTextFieldDetector {

    private static final String TEXT_FIELD_CLASS = "com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget";
    private static final Map<String, String[]> SCREEN_FIELD_WHITELIST = Collections.emptyMap();

    public ModularUi1TextFieldDetector() {
        this(SCREEN_FIELD_WHITELIST);
    }

    ModularUi1TextFieldDetector(Map<String, String[]> screenFieldWhitelist) {
        super(TEXT_FIELD_CLASS, screenFieldWhitelist);
    }
}
