package com.github.skystardust.inputmethodblockergtnh.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModSearchTextFieldDetector extends WhitelistedReflectiveTextFieldDetector {

    private static final Map<String, String[]> INSTANCE_FIELD_WHITELIST = createInstanceFieldWhitelist();
    private static final List<StaticFieldBinding> STATIC_FIELD_WHITELIST = createStaticFieldWhitelist();
    private static final Set<String> TEXT_FIELD_CLASSES = createTextFieldClasses();
    private static final Set<String> FOCUS_METHOD_NAMES = createFocusMethodNames();

    public ModSearchTextFieldDetector() {
        this(INSTANCE_FIELD_WHITELIST, STATIC_FIELD_WHITELIST, TEXT_FIELD_CLASSES, FOCUS_METHOD_NAMES);
    }

    ModSearchTextFieldDetector(
        Map<String, String[]> instanceFieldWhitelist,
        List<StaticFieldBinding> staticFieldWhitelist,
        Set<String> textFieldClasses,
        Set<String> focusMethodNames) {
        super(textFieldClasses, instanceFieldWhitelist, staticFieldWhitelist, focusMethodNames);
    }

    private static Map<String, String[]> createInstanceFieldWhitelist() {
        Map<String, String[]> screens = new HashMap<>();
        screens.put("com.gtnewhorizons.angelica.client.gui.FontConfigScreen", new String[] { "searchBox", "testArea" });
        screens.put(
            "me.flashyreese.mods.reeses_sodium_options.client.gui.ReeseSodiumVideoOptionsScreen",
            new String[] { "searchTextField" });
        screens.put("codechicken.nei.GuiPotionCreator", new String[] { "durationField" });
        screens.put("serverutils.lib.gui.misc.GuiButtonListBase", new String[] { "searchBox" });
        screens.put("serverutils.lib.gui.misc.GuiSelectItemStack", new String[] { "searchBox" });
        screens.put("serverutils.client.gui.teams.GuiCreateTeam", new String[] { "textBoxId" });
        screens.put("serverutils.client.gui.ranks.GuiAddRank", new String[] { "textBoxId" });
        screens.put("serverutils.lib.gui.misc.GuiEditConfigValue", new String[] { "textBox" });
        return Collections.unmodifiableMap(screens);
    }

    private static List<StaticFieldBinding> createStaticFieldWhitelist() {
        List<StaticFieldBinding> bindings = new ArrayList<>();
        bindings.add(
            new StaticFieldBinding(
                "net.minecraft.client.gui.inventory.GuiContainer",
                "codechicken.nei.LayoutManager",
                new String[] { "searchField", "quantity" }));
        bindings.add(
            new StaticFieldBinding(
                "codechicken.nei.recipe.GuiRecipe",
                "codechicken.nei.recipe.GuiRecipe",
                new String[] { "searchField" }));
        return Collections.unmodifiableList(bindings);
    }

    private static Set<String> createTextFieldClasses() {
        Set<String> classes = new HashSet<>();
        classes.add("net.minecraft.client.gui.GuiTextField");
        classes.add("me.flashyreese.mods.reeses_sodium_options.client.gui.frame.components.SearchTextFieldComponent");
        classes.add("codechicken.nei.TextField");
        classes.add("codechicken.core.gui.GuiCCTextField");
        classes.add("serverutils.lib.gui.TextBox");
        return Collections.unmodifiableSet(classes);
    }

    private static Set<String> createFocusMethodNames() {
        Set<String> methods = new HashSet<>();
        methods.add("isFocused");
        methods.add("focused");
        return Collections.unmodifiableSet(methods);
    }
}
