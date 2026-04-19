package com.github.skystardust.inputmethodblockergtnh.ime;

import java.io.File;

@FunctionalInterface
public interface NativeLibraryLoader {

    void load(File libraryFile);
}
