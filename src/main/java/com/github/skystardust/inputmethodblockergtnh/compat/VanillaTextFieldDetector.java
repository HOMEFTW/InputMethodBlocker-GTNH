package com.github.skystardust.inputmethodblockergtnh.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;

public class VanillaTextFieldDetector implements FocusDetector {

    private static final Set<String> ALWAYS_ENABLED_SCREENS = createAlwaysEnabledScreens();
    private static final Map<String, String[]> FOCUSED_TEXT_FIELD_SCREENS = createFocusedTextFieldScreens();

    private final Set<String> alwaysEnabledScreens;
    private final Map<String, String[]> focusedTextFieldScreens;

    public VanillaTextFieldDetector() {
        this(ALWAYS_ENABLED_SCREENS, FOCUSED_TEXT_FIELD_SCREENS);
    }

    VanillaTextFieldDetector(Set<String> alwaysEnabledScreens, Map<String, String[]> focusedTextFieldScreens) {
        this.alwaysEnabledScreens = new HashSet<>(alwaysEnabledScreens);
        this.focusedTextFieldScreens = new HashMap<>(focusedTextFieldScreens);
    }

    @Override
    public boolean hasFocusedTextInput(Object screen) {
        if (screen == null) {
            return false;
        }

        if (matchesAnyClass(screen, alwaysEnabledScreens)) {
            return true;
        }

        for (Map.Entry<String, String[]> entry : focusedTextFieldScreens.entrySet()) {
            if (matchesClass(screen, entry.getKey())) {
                return hasFocusedField(screen, entry.getValue());
            }
        }

        return false;
    }

    private boolean hasFocusedField(Object screen, String[] fieldNames) {
        Object fieldValue = findFieldValue(screen, fieldNames);
        return fieldValue != null && invokeBoolean(fieldValue, "isFocused");
    }

    private Object findFieldValue(Object target, String[] fieldNames) {
        for (String fieldName : fieldNames) {
            for (Class<?> type = target.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(target);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        return null;
    }

    private boolean invokeBoolean(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return Boolean.TRUE.equals(method.invoke(target));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private boolean matchesAnyClass(Object screen, Set<String> classNames) {
        for (String className : classNames) {
            if (matchesClass(screen, className)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesClass(Object screen, String className) {
        for (Class<?> type = screen.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            if (className.equals(type.getName())) {
                return true;
            }
        }

        return false;
    }

    private static Set<String> createAlwaysEnabledScreens() {
        Set<String> screens = new HashSet<>();
        Collections.addAll(
            screens,
            "net.minecraft.client.gui.GuiChat",
            "net.minecraft.client.gui.inventory.GuiEditSign",
            "net.minecraft.client.gui.GuiCommandBlock",
            "net.minecraft.client.gui.GuiCreateWorld",
            "net.minecraft.client.gui.GuiScreenBook",
            "net.minecraft.client.gui.GuiRenameWorld",
            "net.minecraft.client.gui.GuiScreenAddServer",
            "net.minecraft.client.gui.GuiScreenServerList");
        return Collections.unmodifiableSet(screens);
    }

    private static Map<String, String[]> createFocusedTextFieldScreens() {
        Map<String, String[]> screens = new HashMap<>();
        screens.put("net.minecraft.client.gui.GuiRepair", new String[] { "field_147091_w" });
        screens.put("net.minecraft.client.gui.inventory.GuiContainerCreative", new String[] { "searchField", "field_147062_A" });
        return Collections.unmodifiableMap(screens);
    }
}
