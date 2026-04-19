package com.github.skystardust.inputmethodblockergtnh.ime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class PlatformInfoTest {

    @Test
    void identifiesWindows64BitRuntimeAsSupported() {
        Properties properties = new Properties();
        properties.setProperty("os.name", "Windows 11");
        properties.setProperty("os.arch", "amd64");

        PlatformInfo info = PlatformInfo.from(properties);

        assertTrue(info.isWindows());
        assertTrue(info.supportsNativeIme());
        assertEquals("InputMethodBlocker-Natives-x64.dll", info.nativeResourceName());
    }

    @Test
    void rejectsWindows32BitRuntimeForTheNewNativeLayer() {
        Properties properties = new Properties();
        properties.setProperty("os.name", "Windows 7");
        properties.setProperty("os.arch", "x86");

        PlatformInfo info = PlatformInfo.from(properties);

        assertTrue(info.isWindows());
        assertFalse(info.supportsNativeIme());
        assertThrows(IllegalStateException.class, info::nativeResourceName);
    }

    @Test
    void rejectsNonWindowsRuntimes() {
        Properties properties = new Properties();
        properties.setProperty("os.name", "Linux");
        properties.setProperty("os.arch", "amd64");

        PlatformInfo info = PlatformInfo.from(properties);

        assertFalse(info.isWindows());
        assertFalse(info.supportsNativeIme());
    }
}
