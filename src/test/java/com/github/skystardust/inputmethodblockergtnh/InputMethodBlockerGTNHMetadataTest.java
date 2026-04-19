package com.github.skystardust.inputmethodblockergtnh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

class InputMethodBlockerGTNHMetadataTest {

    @Test
    void exposesStableModMetadataAndShipsOnlyTheX64NativeLibrary() throws Exception {
        assertEquals("inputmethodblockergtnh", InputMethodBlockerGTNH.MODID);
        assertEquals("InputMethodBlocker-GTNH", InputMethodBlockerGTNH.MOD_NAME);

        try (InputStream x86 = getClass().getClassLoader().getResourceAsStream("InputMethodBlocker-Natives-x86.dll");
            InputStream x64 = getClass().getClassLoader().getResourceAsStream("InputMethodBlocker-Natives-x64.dll")) {
            assertNull(x86, "x86 native library should no longer be packaged");
            assertNotNull(x64, "x64 native library must be packaged");
        }
    }
}
