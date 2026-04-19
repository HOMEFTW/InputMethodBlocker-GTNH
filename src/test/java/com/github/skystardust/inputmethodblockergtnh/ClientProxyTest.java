package com.github.skystardust.inputmethodblockergtnh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.github.skystardust.inputmethodblockergtnh.compat.VanillaTextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.ConditionalFocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;

class ClientProxyTest {

    @Test
    void keepsOnlyVanillaDetectorWhenNoCompatModsOrClassesArePresent() {
        ClientProxy proxy = new ClientProxy();

        List<FocusDetector> detectors = proxy.createFocusDetectors(mod -> false, className -> false);

        assertEquals(1, detectors.size());
        assertInstanceOf(VanillaTextFieldDetector.class, detectors.get(0));
    }

    @Test
    void addsCompatDetectorsOnlyWhenMatchingModsOrClassesArePresent() {
        ClientProxy proxy = new ClientProxy();
        Predicate<String> loadedMods = modId -> "angelica".equals(modId) || "appliedenergistics2".equals(modId);
        Predicate<String> presentClasses =
            className -> "com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget".equals(className);

        List<FocusDetector> detectors = proxy.createFocusDetectors(loadedMods, presentClasses);

        assertEquals(4, detectors.size());
        assertInstanceOf(VanillaTextFieldDetector.class, detectors.get(0));
        assertEquals("ModSearchTextFieldDetector", detectors.get(1).getClass().getSimpleName());
        assertInstanceOf(ConditionalFocusDetector.class, detectors.get(2));
        assertInstanceOf(ConditionalFocusDetector.class, detectors.get(3));
    }
}
