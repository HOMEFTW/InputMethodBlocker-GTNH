package com.github.skystardust.inputmethodblockergtnh.compat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModularUi1TextFieldDetector extends WhitelistedReflectiveTextFieldDetector {

    private static final String TEXT_FIELD_CLASS =
        "com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget";
    private static final String BASE_TEXT_FIELD_CLASS =
        "com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget";
    private static final Set<String> TEXT_FIELD_CLASSES = createTextFieldClasses();
    private static final Map<String, String[]> SCREEN_FIELD_WHITELIST = createScreenFieldWhitelist();

    public ModularUi1TextFieldDetector() {
        this(SCREEN_FIELD_WHITELIST);
    }

    ModularUi1TextFieldDetector(Map<String, String[]> screenFieldWhitelist) {
        super(TEXT_FIELD_CLASSES, screenFieldWhitelist);
    }

    private static Set<String> createTextFieldClasses() {
        Set<String> textFieldClasses = new HashSet<>();
        textFieldClasses.add(TEXT_FIELD_CLASS);
        textFieldClasses.add(BASE_TEXT_FIELD_CLASS);
        return Collections.unmodifiableSet(textFieldClasses);
    }

    private static Map<String, String[]> createScreenFieldWhitelist() {
        Map<String, String[]> screens = new HashMap<>();
        screens.put(
            "com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui",
            new String[] { "context.cursor.focused" });
        return screens;
    }
}
