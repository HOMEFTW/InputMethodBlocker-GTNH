# Native IME Rewrite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy Windows JNI DLL with a new Windows x64-only native implementation that truly blocks IME on non-white-list screens and restores it for white-list text inputs.

**Architecture:** Keep the Java bridge shape intact so `WindowsImeBridge` still drives two JNI methods through `NativeUtils`, but tighten the Java contract to Windows x64 only. Add a new native C++ source file plus a PowerShell build script that compiles a replacement `InputMethodBlocker-Natives-x64.dll` directly into `src/main/resources`, then verify the new jar with existing automated tests plus Windows manual runtime checks.

**Tech Stack:** Java 17 on GTNH 1.7.10, Forge/FML, JUnit 5, Windows Win32 IMM APIs, JNI, MSVC Build Tools, PowerShell.

---

## File Map

### Java contract and packaging

- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfo.java`
- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridge.java`
- Modify: `src/test/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNHMetadataTest.java`
- Modify: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfoTest.java`
- Modify: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridgeTest.java`
- Delete: `src/main/resources/InputMethodBlocker-Natives-x86.dll`

### Native rewrite

- Create: `native/windows-x64/InputMethodBlocker-Natives-x64.cpp`
- Create: `native/windows-x64/build-native.ps1`
- Overwrite via script: `src/main/resources/InputMethodBlocker-Natives-x64.dll`

### Tracking docs

- Modify: `log.md`
- Modify: `ToDOLIST.md`
- Modify: `context.md`

## Key Decisions Locked In

- Preserve the JNI class and method names in `com.github.skystardust.InputMethodBlocker.NativeUtils`.
- Support only Windows x64 for the rewritten native layer.
- Keep all white-list detectors unchanged during this rewrite.
- Build the replacement DLL directly into `src/main/resources` so existing jar packaging continues to work.
- Use window-level IME context detach and restore instead of keyboard hooks or repeated open-status toggling only.

### Task 1: Tighten the Java Contract to Windows x64 Only

**Files:**
- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfo.java`
- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridge.java`
- Modify: `src/test/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNHMetadataTest.java`
- Modify: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfoTest.java`
- Modify: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridgeTest.java`
- Delete: `src/main/resources/InputMethodBlocker-Natives-x86.dll`

- [ ] **Step 1: Write the failing tests for x64-only packaging and platform support**

```java
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
```

```java
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
```

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

class WindowsImeBridgeTest {

    @Test
    void becomesAvailableAfterSuccessfulLoadAndDebouncesDuplicateTrueState() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows64(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);
        bridge.setImeActive(true);
        bridge.setImeActive(false);

        assertTrue(bridge.isAvailable());
        assertEquals(1, loader.loads);
        assertEquals(1, bindings.activeCalls);
        assertEquals(2, bindings.inactiveCalls);
    }

    @Test
    void staysDisabledOnWindows32BitRuntime() {
        RecordingBindings bindings = new RecordingBindings();
        RecordingLoader loader = new RecordingLoader();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows32(),
            resourceName -> new File(resourceName),
            loader,
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);

        assertFalse(bridge.isAvailable());
        assertEquals(0, loader.loads);
        assertEquals(0, bindings.activeCalls);
        assertEquals(0, bindings.inactiveCalls);
    }

    private static final class RecordingLoader implements NativeLibraryLoader {

        private int loads;

        @Override
        public void load(File libraryFile) {
            loads++;
        }
    }

    private static final class RecordingBindings implements NativeImeBindings {

        private int activeCalls;
        private int inactiveCalls;

        @Override
        public void activeInputMethod(String windowName) {
            activeCalls++;
        }

        @Override
        public void inactiveInputMethod(String windowName) {
            inactiveCalls++;
        }
    }
}
```

- [ ] **Step 2: Run the targeted tests to verify they fail**

Run:

```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNHMetadataTest --tests com.github.skystardust.inputmethodblockergtnh.ime.PlatformInfoTest --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest
```

Expected: FAIL because the project still packages `InputMethodBlocker-Natives-x86.dll`, `PlatformInfo` does not expose `supportsNativeIme()` or `windows32()`, and `WindowsImeBridge` still treats any Windows runtime as supported.

- [ ] **Step 3: Implement the x64-only Java contract**

```java
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
```

```java
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
                logNativeCall("activeInputMethod", activeCallCount, true);
                bindings.activeInputMethod("");
            } else {
                inactiveCallCount++;
                logNativeCall("inactiveInputMethod", inactiveCallCount, false);
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
```

Delete:

```text
src/main/resources/InputMethodBlocker-Natives-x86.dll
```

- [ ] **Step 4: Run the same targeted tests to verify they pass**

Run:

```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNHMetadataTest --tests com.github.skystardust.inputmethodblockergtnh.ime.PlatformInfoTest --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest
```

Expected: PASS.

- [ ] **Step 5: Record the checkpoint status**

Run:

```powershell
git -C D:\Code\InputMethodBlocker-GTNH rev-parse --is-inside-work-tree
```

Expected: the command still fails with `not a git repository`, so do not attempt a commit in this workspace.

### Task 2: Add the Native Build Pipeline

**Files:**
- Create: `native/windows-x64/build-native.ps1`
- Create: `native/windows-x64/InputMethodBlocker-Natives-x64.cpp`
- Overwrite via script: `src/main/resources/InputMethodBlocker-Natives-x64.dll`

- [ ] **Step 1: Create the failing native build command**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File native/windows-x64/build-native.ps1
```

Expected: FAIL because the native source file and build script do not exist yet.

- [ ] **Step 2: Add the native build script**

```powershell
param(
    [string]$Source = "$PSScriptRoot\InputMethodBlocker-Natives-x64.cpp",
    [string]$Output = "$PSScriptRoot\..\..\src\main\resources\InputMethodBlocker-Natives-x64.dll"
)

$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME must be set before building the native DLL."
}

$javaInclude = Join-Path $env:JAVA_HOME "include"
$javaWin32Include = Join-Path $javaInclude "win32"

if (-not (Test-Path $Source)) {
    throw "Native source file not found: $Source"
}

$vswhere = Join-Path ${env:ProgramFiles(x86)} "Microsoft Visual Studio\Installer\vswhere.exe"
if (-not (Test-Path $vswhere)) {
    throw "vswhere.exe not found. Install Visual Studio Build Tools with C++ support."
}

$installationPath = & $vswhere -latest -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath
if (-not $installationPath) {
    throw "No Visual Studio C++ Build Tools installation with x64 tools was found."
}

$vcvars = Join-Path $installationPath "VC\Auxiliary\Build\vcvars64.bat"
if (-not (Test-Path $vcvars)) {
    throw "vcvars64.bat not found: $vcvars"
}

$outputDir = Split-Path -Parent $Output
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$command = @(
    'call "' + $vcvars + '"',
    'cl.exe /nologo /LD /EHsc /O2 /I"' + $javaInclude + '" /I"' + $javaWin32Include + '" "' + $Source + '" /link /OUT:"' + $Output + '" imm32.lib user32.lib'
) -join " && "

cmd.exe /c $command

if ($LASTEXITCODE -ne 0) {
    throw "Native build failed with exit code $LASTEXITCODE"
}

if (-not (Test-Path $Output)) {
    throw "Native build reported success but no DLL was produced: $Output"
}

Write-Host "Built native DLL: $Output"
```

- [ ] **Step 3: Add the native source file with safe JNI exports and window-level IME detach logic**

```cpp
#include <jni.h>
#include <windows.h>
#include <imm.h>

#include <mutex>

#pragma comment(lib, "imm32.lib")
#pragma comment(lib, "user32.lib")

namespace {

struct ImeState {
    HWND window;
    HIMC detachedContext;
    bool detached;
};

ImeState g_state = { nullptr, nullptr, false };
std::mutex g_mutex;

HWND resolveTargetWindow() {
    HWND hwnd = GetForegroundWindow();
    if (hwnd == nullptr) return nullptr;

    DWORD processId = 0;
    GetWindowThreadProcessId(hwnd, &processId);
    return processId == GetCurrentProcessId() ? hwnd : nullptr;
}

void resetState() {
    g_state.window = nullptr;
    g_state.detachedContext = nullptr;
    g_state.detached = false;
}

void closeImeOnContext(HIMC context) {
    if (context == nullptr) return;
    ImmNotifyIME(context, NI_COMPOSITIONSTR, CPS_CANCEL, 0);
    ImmSetOpenStatus(context, FALSE);
}

void closeImeOnWindow(HWND hwnd) {
    if (hwnd == nullptr) return;
    HIMC context = ImmGetContext(hwnd);
    if (context == nullptr) return;

    closeImeOnContext(context);
    ImmReleaseContext(hwnd, context);
}

void restoreDetachedWindow() {
    if (!g_state.detached) {
        resetState();
        return;
    }

    if (g_state.window != nullptr && IsWindow(g_state.window) && g_state.detachedContext != nullptr) {
        ImmAssociateContext(g_state.window, g_state.detachedContext);
    }

    resetState();
}

void detachImeFromWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    if (g_state.detached && g_state.window == hwnd) {
        return;
    }

    if (g_state.detached && g_state.window != hwnd) {
        restoreDetachedWindow();
    }

    closeImeOnWindow(hwnd);

    HIMC previousContext = ImmAssociateContext(hwnd, nullptr);
    if (previousContext != nullptr) {
        g_state.window = hwnd;
        g_state.detachedContext = previousContext;
        g_state.detached = true;
    } else {
        resetState();
    }
}

void attachImeToWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    if (g_state.detached && g_state.detachedContext != nullptr) {
        ImmAssociateContext(hwnd, g_state.detachedContext);
    }

    HIMC context = ImmGetContext(hwnd);
    if (context != nullptr) {
        ImmSetOpenStatus(context, TRUE);
        ImmReleaseContext(hwnd, context);
    }

    resetState();
}

}  // namespace

extern "C" {

JNIEXPORT void JNICALL Java_com_github_skystardust_InputMethodBlocker_NativeUtils_inactiveInputMethod(
    JNIEnv*,
    jclass,
    jstring) {
    std::lock_guard<std::mutex> guard(g_mutex);
    HWND hwnd = resolveTargetWindow();
    if (hwnd == nullptr) return;
    detachImeFromWindow(hwnd);
}

JNIEXPORT void JNICALL Java_com_github_skystardust_InputMethodBlocker_NativeUtils_activeInputMethod(
    JNIEnv*,
    jclass,
    jstring) {
    std::lock_guard<std::mutex> guard(g_mutex);
    HWND hwnd = resolveTargetWindow();
    if (hwnd == nullptr) {
        if (g_state.detached && g_state.window != nullptr && IsWindow(g_state.window)) {
            hwnd = g_state.window;
        } else {
            return;
        }
    }
    attachImeToWindow(hwnd);
}

}

BOOL APIENTRY DllMain(HMODULE, DWORD reason, LPVOID) {
    if (reason == DLL_PROCESS_DETACH) {
        std::lock_guard<std::mutex> guard(g_mutex);
        restoreDetachedWindow();
    }
    return TRUE;
}
```

- [ ] **Step 4: Run the native build command to verify it now succeeds**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File native/windows-x64/build-native.ps1
```

Expected: PASS and `src/main/resources/InputMethodBlocker-Natives-x64.dll` is overwritten with a newly built binary.

- [ ] **Step 5: Verify the rewritten DLL exports the expected JNI symbols**

Run:

```powershell
$dll = Resolve-Path .\src\main\resources\InputMethodBlocker-Natives-x64.dll
$vswhere = Join-Path ${env:ProgramFiles(x86)} "Microsoft Visual Studio\Installer\vswhere.exe"
$installationPath = & $vswhere -latest -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath
$vcvars = Join-Path $installationPath "VC\Auxiliary\Build\vcvars64.bat"
cmd.exe /c ('call "' + $vcvars + '" && dumpbin /exports "' + $dll + '"')
```

Expected: the export list contains both:
- `Java_com_github_skystardust_InputMethodBlocker_NativeUtils_inactiveInputMethod`
- `Java_com_github_skystardust_InputMethodBlocker_NativeUtils_activeInputMethod`

### Task 3: Harden the Native Window Lifecycle Behavior

**Files:**
- Modify: `native/windows-x64/InputMethodBlocker-Natives-x64.cpp`
- Overwrite via script: `src/main/resources/InputMethodBlocker-Natives-x64.dll`

- [ ] **Step 1: Write down the failing manual repro that the new DLL must fix**

Run:

```text
1. Launch GTNH with the current jar.
2. Stay on the main menu or enter the world without focusing a white-list text field.
3. Press Shift repeatedly to try toggling the IME.
4. Observe that IME still becomes active for the Minecraft window.
```

Expected: this is the current failure and the reason for the native rewrite.

- [ ] **Step 2: Expand the C++ implementation to handle window changes and best-effort restore**

```cpp
#include <jni.h>
#include <windows.h>
#include <imm.h>

#include <mutex>

#pragma comment(lib, "imm32.lib")
#pragma comment(lib, "user32.lib")

namespace {

struct ImeState {
    HWND window;
    HIMC detachedContext;
    bool detached;
};

ImeState g_state = { nullptr, nullptr, false };
std::mutex g_mutex;

HWND resolveTargetWindow() {
    HWND hwnd = GetForegroundWindow();
    if (hwnd == nullptr) return nullptr;

    DWORD processId = 0;
    GetWindowThreadProcessId(hwnd, &processId);
    if (processId != GetCurrentProcessId()) return nullptr;

    return hwnd;
}

void resetState() {
    g_state.window = nullptr;
    g_state.detachedContext = nullptr;
    g_state.detached = false;
}

void closeImeOnContext(HIMC context) {
    if (context == nullptr) return;
    ImmNotifyIME(context, NI_COMPOSITIONSTR, CPS_CANCEL, 0);
    ImmSetOpenStatus(context, FALSE);
}

void closeImeOnWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    HIMC context = ImmGetContext(hwnd);
    if (context == nullptr) return;

    closeImeOnContext(context);
    ImmReleaseContext(hwnd, context);
}

void restoreDetachedWindow() {
    if (!g_state.detached) {
        resetState();
        return;
    }

    if (g_state.window != nullptr && IsWindow(g_state.window) && g_state.detachedContext != nullptr) {
        ImmAssociateContext(g_state.window, g_state.detachedContext);
    }

    resetState();
}

void ensureDetachedOnWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    if (g_state.detached && g_state.window == hwnd) {
        return;
    }

    if (g_state.detached && g_state.window != hwnd) {
        restoreDetachedWindow();
    }

    closeImeOnWindow(hwnd);

    HIMC previousContext = ImmAssociateContext(hwnd, nullptr);
    if (previousContext == nullptr) {
        resetState();
        return;
    }

    g_state.window = hwnd;
    g_state.detachedContext = previousContext;
    g_state.detached = true;
}

void restoreAndOpenOnWindow(HWND hwnd) {
    if (hwnd == nullptr) return;

    if (g_state.detached && g_state.detachedContext != nullptr) {
        ImmAssociateContext(hwnd, g_state.detachedContext);
    }

    HIMC context = ImmGetContext(hwnd);
    if (context != nullptr) {
        ImmSetOpenStatus(context, TRUE);
        ImmReleaseContext(hwnd, context);
    }

    resetState();
}

}  // namespace

extern "C" {

JNIEXPORT void JNICALL Java_com_github_skystardust_InputMethodBlocker_NativeUtils_inactiveInputMethod(
    JNIEnv*,
    jclass,
    jstring) {
    std::lock_guard<std::mutex> guard(g_mutex);
    HWND hwnd = resolveTargetWindow();
    if (hwnd == nullptr) return;
    ensureDetachedOnWindow(hwnd);
}

JNIEXPORT void JNICALL Java_com_github_skystardust_InputMethodBlocker_NativeUtils_activeInputMethod(
    JNIEnv*,
    jclass,
    jstring) {
    std::lock_guard<std::mutex> guard(g_mutex);
    HWND hwnd = resolveTargetWindow();

    if (hwnd == nullptr && g_state.detached && g_state.window != nullptr && IsWindow(g_state.window)) {
        hwnd = g_state.window;
    }

    if (hwnd == nullptr) return;
    restoreAndOpenOnWindow(hwnd);
}

}

BOOL APIENTRY DllMain(HMODULE, DWORD reason, LPVOID) {
    if (reason == DLL_PROCESS_DETACH) {
        std::lock_guard<std::mutex> guard(g_mutex);
        restoreDetachedWindow();
    }
    return TRUE;
}
```

- [ ] **Step 3: Rebuild the native DLL**

Run:

```powershell
powershell -ExecutionPolicy Bypass -File native/windows-x64/build-native.ps1
```

Expected: PASS.

- [ ] **Step 4: Run the focused Java regression tests**

Run:

```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.focus.InputFocusServiceTest --tests com.github.skystardust.inputmethodblockergtnh.client.ClientEventHandlerTest --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest
```

Expected: PASS. These tests do not prove Win32 behavior directly, but they verify the Java-side contract that drives the native layer.

- [ ] **Step 5: Perform the Windows manual validation pass**

Run:

```text
1. Start the client with the rebuilt jar.
2. On the main menu, press Shift repeatedly and verify IME does not stay active.
3. Enter a world and repeat the same Shift test with no text field focused.
4. Open a vanilla white-list text field and verify Chinese input works.
5. Open an AE2, Angelica, NEI, ServerUtilities, MUI1, and MUI2 white-list field and verify Chinese input works.
6. Exit each text field and verify IME is blocked again.
7. Alt-Tab during both blocked and allowed states and verify the client stays responsive.
```

Expected: all checks pass. If any fail, return to the native source file only and avoid changing detectors during this rewrite.

### Task 4: Build the Jar and Refresh Project Tracking

**Files:**
- Modify: `log.md`
- Modify: `ToDOLIST.md`
- Modify: `context.md`

- [ ] **Step 1: Run the full automated verification suite**

Run:

```powershell
./gradlew.bat test
./gradlew.bat compileJava
./gradlew.bat assemble
```

Expected: PASS for all three commands. Run them sequentially, not in parallel.

- [ ] **Step 2: Confirm the rebuilt jar exists**

Run:

```powershell
Get-ChildItem .\build\libs\inputmethodblockergtnh-*.jar | Select-Object Name, Length, LastWriteTime
```

Expected: at least one freshly updated jar is listed.

- [ ] **Step 3: Update `log.md` in Chinese**

```markdown
## 2026-04-20：重写 Windows x64 native IME DLL

### 已完成
- 将 native 支持范围收敛为 Windows x64，并移除 `InputMethodBlocker-Natives-x86.dll`
- 新增 `native/windows-x64/build-native.ps1` 与 `InputMethodBlocker-Natives-x64.cpp`
- 以窗口级 `ImmAssociateContext(hwnd, NULL)` / 恢复原始 `HIMC` 的方式替换旧 DLL 逻辑
- 重新构建 `src/main/resources/InputMethodBlocker-Natives-x64.dll`
- 重新通过 `./gradlew.bat test`、`./gradlew.bat compileJava` 与 `./gradlew.bat assemble`

### 遇到的问题
- **旧 DLL 在 Java 17-25 + GTNH 环境下无效**：Java 侧持续调用 `inactiveInputMethod`，但 IME 仍可被 `Shift` 切回
- **native 重写需要严格限制作用窗口**：只能操作当前 Java 进程前台窗口，避免影响其他程序

### 已做决定
- 保持 JNI ABI 不变，仅替换 x64 DLL
- 非白名单状态下走窗口级 IME context detach，而不是键盘热键拦截
```

- [ ] **Step 4: Update `ToDOLIST.md` in Chinese**

```markdown
## 当前计划
- [ ] 在真实 Windows GTNH 客户端继续补充 MUI1 / MUI2 白名单条目
- [ ] 根据实机结果继续补充 AE2、Angelica、NEI、ServerUtilities 白名单条目
- [ ] 排查并修复 `spotlessCheck` 的 Spotless/脚手架异常

## 已完成
- [x] 重写 Windows x64 native IME DLL，替换旧 `InputMethodBlocker-Natives-x64.dll`
- [x] 将 native 支持范围收敛为 Windows x64，并移除 x86 DLL 分发
```

- [ ] **Step 5: Update `context.md` in Chinese**

```markdown
## 已实现代码模块
- native 源码：`native/windows-x64/InputMethodBlocker-Natives-x64.cpp`
- native 构建脚本：`native/windows-x64/build-native.ps1`

## 依赖与兼容目标
- Windows native 支持已收敛为 `InputMethodBlocker-Natives-x64.dll`

## 架构说明
- `WindowsImeBridge` 继续复用 `com.github.skystardust.InputMethodBlocker.NativeUtils`
- 新 DLL 通过窗口级 IME context detach / restore 阻断非白名单状态下的输入法切换
- 白名单检测层与 compat 层保持不变，本次重写仅替换 native 实现
```

- [ ] **Step 6: Record the workspace VCS status**

Run:

```powershell
git -C D:\Code\InputMethodBlocker-GTNH rev-parse --is-inside-work-tree
```

Expected: still fails with `not a git repository`, so close out without a commit.

## Final Verification Checklist

- [ ] `./gradlew.bat test`
- [ ] `./gradlew.bat compileJava`
- [ ] `./gradlew.bat assemble`
- [ ] `powershell -ExecutionPolicy Bypass -File native/windows-x64/build-native.ps1`
- [ ] Main menu IME blocking manually verified on Windows
- [ ] In-game non-input IME blocking manually verified on Windows
- [ ] Vanilla, AE2, Angelica, NEI, ServerUtilities, MUI1, and MUI2 white-list fields manually verified on Windows
- [ ] `log.md`, `ToDOLIST.md`, and `context.md` updated in Chinese
