package com.github.skystardust.inputmethodblockergtnh.ime;

import java.util.Locale;
import java.util.Properties;

public final class PlatformInfo {

    private final String osName;
    private final String osArch;
    private final boolean windows;

    private PlatformInfo(String osName, String osArch, boolean windows) {
        this.osName = osName;
        this.osArch = osArch;
        this.windows = windows;
    }

    public static PlatformInfo from(Properties properties) {
        String osName = properties.getProperty("os.name", "");
        String osArch = properties.getProperty("os.arch", "");
        boolean windows = osName.toLowerCase(Locale.ROOT).contains("win");
        return new PlatformInfo(osName, osArch, windows);
    }

    public static PlatformInfo windows64() {
        return new PlatformInfo("Windows", "amd64", true);
    }

    public static PlatformInfo windows32() {
        return new PlatformInfo("Windows", "x86", true);
    }

    public static PlatformInfo nonWindows(String osName, String osArch) {
        return new PlatformInfo(osName, osArch, false);
    }

    public boolean isWindows() {
        return windows;
    }

    public boolean supportsNativeIme() {
        return windows && osArch.contains("64");
    }

    public String nativeResourceName() {
        if (!supportsNativeIme()) {
            throw new IllegalStateException("unsupported native runtime: " + this);
        }
        return "InputMethodBlocker-Natives-x64.dll";
    }

    @Override
    public String toString() {
        return "PlatformInfo{" + "osName='" + osName + '\'' + ", osArch='" + osArch + '\'' + ", windows=" + windows
            + '}';
    }
}
