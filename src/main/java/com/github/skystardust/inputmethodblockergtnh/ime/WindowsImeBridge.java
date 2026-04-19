package com.github.skystardust.inputmethodblockergtnh.ime;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

import com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNH;

public class WindowsImeBridge implements ImeBridge {

    private final PlatformInfo platformInfo;
    private final Function<String, File> extractor;
    private final NativeLibraryLoader loader;
    private final NativeImeBindings bindings;

    private boolean initialized;
    private boolean available;
    private Boolean currentActive;
    private int activeCallCount;
    private int inactiveCallCount;
    private boolean loggedUnavailableCall;

    public WindowsImeBridge(PlatformInfo platformInfo, Function<String, File> extractor, NativeLibraryLoader loader,
        NativeImeBindings bindings) {
        this.platformInfo = Objects.requireNonNull(platformInfo);
        this.extractor = Objects.requireNonNull(extractor);
        this.loader = Objects.requireNonNull(loader);
        this.bindings = Objects.requireNonNull(bindings);
    }

    @Override
    public void initialize() {
        if (initialized) return;
        initialized = true;

        if (!platformInfo.supportsNativeIme()) {
            InputMethodBlockerGTNH.LOG.info("IME bridge disabled for unsupported platform: {}", platformInfo);
            return;
        }

        try {
            File library = extractor.apply(platformInfo.nativeResourceName());
            loader.load(library);
            bindings.inactiveInputMethod("");
            available = true;
            currentActive = false;
            inactiveCallCount = 1;
            InputMethodBlockerGTNH.LOG.info(
                "loaded IME bridge from {} and applied initial inactive state",
                library.getAbsolutePath());
        } catch (Throwable t) {
            available = false;
            currentActive = null;
            InputMethodBlockerGTNH.LOG.warn("failed to initialize IME bridge", t);
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setImeActive(boolean active) {
        if (!available) {
            if (!loggedUnavailableCall) {
                loggedUnavailableCall = true;
                InputMethodBlockerGTNH.LOG.warn("ignoring IME state change because bridge is unavailable");
            }
            return;
        }
        if (active && Objects.equals(currentActive, Boolean.TRUE)) return;

        try {
            if (active) {
                activeCallCount++;
                logNativeCall("activeInputMethod", activeCallCount, active);
                bindings.activeInputMethod("");
            } else {
                inactiveCallCount++;
                logNativeCall("inactiveInputMethod", inactiveCallCount, active);
                bindings.inactiveInputMethod("");
            }
            currentActive = active;
        } catch (Throwable t) {
            available = false;
            currentActive = null;
            InputMethodBlockerGTNH.LOG.warn("disabling IME bridge after native call failure", t);
        }
    }

    private void logNativeCall(String methodName, int callCount, boolean active) {
        if (callCount <= 5 || callCount % 100 == 0) {
            InputMethodBlockerGTNH.LOG.info(
                "calling native {} count={} desiredImeActive={}",
                methodName,
                callCount,
                active);
        }
    }
}
