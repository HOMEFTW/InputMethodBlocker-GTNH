package com.github.skystardust.inputmethodblockergtnh.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;

abstract class WhitelistedReflectiveTextFieldDetector implements FocusDetector {

    private final Set<String> textFieldClassNames;
    private final Map<String, String[]> screenFieldWhitelist;
    private final List<StaticFieldBinding> staticFieldBindings;
    private final Set<String> focusMethodNames;

    protected WhitelistedReflectiveTextFieldDetector(String textFieldClassName, Map<String, String[]> screenFieldWhitelist) {
        this(Collections.singleton(textFieldClassName), screenFieldWhitelist, Collections.<StaticFieldBinding>emptyList());
    }

    protected WhitelistedReflectiveTextFieldDetector(Set<String> textFieldClassNames, Map<String, String[]> screenFieldWhitelist) {
        this(textFieldClassNames, screenFieldWhitelist, Collections.<StaticFieldBinding>emptyList());
    }

    protected WhitelistedReflectiveTextFieldDetector(
        Set<String> textFieldClassNames,
        Map<String, String[]> screenFieldWhitelist,
        List<StaticFieldBinding> staticFieldBindings) {
        this(textFieldClassNames, screenFieldWhitelist, staticFieldBindings, Collections.singleton("isFocused"));
    }

    protected WhitelistedReflectiveTextFieldDetector(
        Set<String> textFieldClassNames,
        Map<String, String[]> screenFieldWhitelist,
        List<StaticFieldBinding> staticFieldBindings,
        Set<String> focusMethodNames) {
        this.textFieldClassNames = new HashSet<>(textFieldClassNames);
        this.screenFieldWhitelist = new HashMap<>(screenFieldWhitelist);
        this.staticFieldBindings = new ArrayList<>(staticFieldBindings);
        this.focusMethodNames = new HashSet<>(focusMethodNames);
    }

    @Override
    public boolean hasFocusedTextInput(Object screen) {
        if (screen == null) {
            return false;
        }

        for (Map.Entry<String, String[]> entry : screenFieldWhitelist.entrySet()) {
            if (matchesClass(screen, entry.getKey()) && hasFocusedWhitelistedField(screen, entry.getValue())) {
                return true;
            }
        }

        for (StaticFieldBinding binding : staticFieldBindings) {
            if (matchesClass(screen, binding.getScreenClassName())
                && hasFocusedStaticField(binding.getOwnerClassName(), binding.getFieldNames())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasFocusedWhitelistedField(Object screen, String[] fieldNames) {
        for (String fieldName : fieldNames) {
            Object fieldValue = findFieldValue(screen, fieldName);
            if (fieldValue != null && isFocusedTextField(fieldValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFocusedStaticField(String ownerClassName, String[] fieldNames) {
        Class<?> ownerClass = loadClass(ownerClassName);
        if (ownerClass == null) {
            return false;
        }

        for (String fieldName : fieldNames) {
            Object fieldValue = findStaticFieldValue(ownerClass, fieldName);
            if (fieldValue != null && isFocusedTextField(fieldValue)) {
                return true;
            }
        }

        return false;
    }

    private Object findFieldValue(Object target, String fieldName) {
        for (Class<?> type = target.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {}
        }

        return null;
    }

    private Object findStaticFieldValue(Class<?> ownerClass, String fieldName) {
        for (Class<?> type = ownerClass; type != null && type != Object.class; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(null);
            } catch (ReflectiveOperationException ignored) {}
        }

        return null;
    }

    private boolean isFocusedTextField(Object candidate) {
        return matchesSupportedTextFieldClass(candidate.getClass()) && invokeFocusMethod(candidate);
    }

    private boolean matchesSupportedTextFieldClass(Class<?> candidateClass) {
        for (Class<?> type = candidateClass; type != null && type != Object.class; type = type.getSuperclass()) {
            if (textFieldClassNames.contains(type.getName())) {
                return true;
            }
        }

        return false;
    }

    private boolean invokeFocusMethod(Object target) {
        for (String methodName : focusMethodNames) {
            Boolean value = invokeBoolean(target, methodName);
            if (value != null) {
                return value.booleanValue();
            }
        }

        return false;
    }

    private Boolean invokeBoolean(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return Boolean.valueOf(Boolean.TRUE.equals(method.invoke(target)));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private boolean matchesClass(Object screen, String className) {
        for (Class<?> type = screen.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            if (className.equals(type.getName())) {
                return true;
            }
        }

        return false;
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    static final class StaticFieldBinding {

        private final String screenClassName;
        private final String ownerClassName;
        private final String[] fieldNames;

        StaticFieldBinding(String screenClassName, String ownerClassName, String[] fieldNames) {
            this.screenClassName = screenClassName;
            this.ownerClassName = ownerClassName;
            this.fieldNames = fieldNames.clone();
        }

        String getScreenClassName() {
            return screenClassName;
        }

        String getOwnerClassName() {
            return ownerClassName;
        }

        String[] getFieldNames() {
            return fieldNames.clone();
        }
    }
}
