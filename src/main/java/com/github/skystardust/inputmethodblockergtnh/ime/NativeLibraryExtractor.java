package com.github.skystardust.inputmethodblockergtnh.ime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeLibraryExtractor {

    public File extract(String resourceName) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new IllegalStateException("missing native resource " + resourceName);
            }

            File file = File.createTempFile("InputMethodBlocker-GTNH-", ".dll");
            Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("failed to extract " + resourceName, e);
        }
    }
}
