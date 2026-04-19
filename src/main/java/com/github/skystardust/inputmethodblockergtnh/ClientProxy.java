package com.github.skystardust.inputmethodblockergtnh;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import com.github.skystardust.inputmethodblockergtnh.client.ClientEventHandler;
import com.github.skystardust.inputmethodblockergtnh.compat.AeTerminalTextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.compat.ModSearchTextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.compat.ModularUi1TextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.compat.ModularUi2TextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.compat.VanillaTextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.ConditionalFocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.InputFocusService;
import com.github.skystardust.inputmethodblockergtnh.ime.LegacyNativeImeBindings;
import com.github.skystardust.inputmethodblockergtnh.ime.NativeLibraryExtractor;
import com.github.skystardust.inputmethodblockergtnh.ime.PlatformInfo;
import com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridge;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ClientEventHandler handler = new ClientEventHandler(
            createBridge(),
            new InputFocusService(createFocusDetectors(this::isCompatModLoaded, this::isCompatClassAvailable)));
        handler.initialize();
        FMLCommonHandler.instance()
            .bus()
            .register(handler);
    }

    List<FocusDetector> createFocusDetectors(
        Predicate<String> isModLoaded,
        Predicate<String> isClassAvailable) {
        List<FocusDetector> detectors = new ArrayList<>(Arrays.<FocusDetector>asList(new VanillaTextFieldDetector()));

        if (isModLoaded.test("angelica") || isModLoaded.test("nei") || isModLoaded.test("NotEnoughItems")
            || isModLoaded.test("serverutilities")) {
            detectors.add(new ModSearchTextFieldDetector());
        }

        if (isModLoaded.test("appliedenergistics2") || isModLoaded.test("ae2thing")) {
            detectors.add(new ConditionalFocusDetector(this::isInGame, new AeTerminalTextFieldDetector()));
        }

        if (isClassAvailable.test("com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget")) {
            detectors.add(new ConditionalFocusDetector(this::isInGame, new ModularUi1TextFieldDetector()));
        }

        if (isClassAvailable.test("com.cleanroommc.modularui.widgets.textfield.TextFieldWidget")) {
            detectors.add(new ConditionalFocusDetector(this::isInGame, new ModularUi2TextFieldDetector()));
        }

        return detectors;
    }

    private WindowsImeBridge createBridge() {
        return new WindowsImeBridge(
            PlatformInfo.from(System.getProperties()),
            resource -> new NativeLibraryExtractor().extract(resource),
            this::loadLibrary,
            new LegacyNativeImeBindings());
    }

    private void loadLibrary(File libraryFile) {
        System.load(libraryFile.getAbsolutePath());
    }

    private boolean isCompatModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }

    private boolean isCompatClassAvailable(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private boolean isInGame() {
        return net.minecraft.client.Minecraft.getMinecraft().theWorld != null;
    }
}
