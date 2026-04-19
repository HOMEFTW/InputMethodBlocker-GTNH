package com.github.skystardust.inputmethodblockergtnh.compat;

import java.util.Collections;
import java.util.Map;

public class ModularUi2TextFieldDetector extends WhitelistedReflectiveTextFieldDetector {

    private static final String TEXT_FIELD_CLASS = "com.cleanroommc.modularui.widgets.textfield.TextFieldWidget";
    private static final Map<String, String[]> SCREEN_FIELD_WHITELIST = Collections.emptyMap();

    public ModularUi2TextFieldDetector() {
        this(SCREEN_FIELD_WHITELIST);
    }

    ModularUi2TextFieldDetector(Map<String, String[]> screenFieldWhitelist) {
        super(TEXT_FIELD_CLASS, screenFieldWhitelist);
    }
}
