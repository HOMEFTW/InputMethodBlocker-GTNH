# InputMethodBlocker-GTNH Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `InputMethodBlocker-GTNH` as a GTNH-native 1.7.10 mod project that works in Java 17-25, reuses the legacy Windows DLLs safely, and enables IME automatically for vanilla, MUI1, and MUI2 text input widgets.

**Architecture:** The mod is split into a thin Forge entry layer (`InputMethodBlockerGTNH`, proxies, client event handler), an IME layer (`ImeBridge`, `WindowsImeBridge`, resource extraction), and a detector layer (`InputFocusService` plus vanilla/MUI1/MUI2 detectors). Reflection-based scanners keep the compatibility surface generic so we do not maintain per-mod allowlists, while safe fallbacks guarantee that non-Windows systems or broken reflection paths degrade without crashing the client.

**Tech Stack:** Java 17 syntax on GTNH 1.7.10, Forge/FML, GTNH convention Gradle plugin, JUnit 5, reflection-based GUI scanning, legacy Windows JNI DLLs.

---

## File Map

### Build and metadata

- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `settings.gradle`
- Create: `src/main/resources/mcmod.info`
- Create: `src/main/resources/assets/inputmethodblockergtnh/lang/en_US.lang`
- Create: `src/main/resources/InputMethodBlocker-Natives-x86.dll`
- Create: `src/main/resources/InputMethodBlocker-Natives-x64.dll`

### Mod entry and client wiring

- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNH.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/CommonProxy.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ClientProxy.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/client/ClientEventHandler.java`

### IME bridge

- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/ImeBridge.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/DisabledImeBridge.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfo.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/NativeLibraryExtractor.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/NativeImeBindings.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridge.java`

### Focus detection

- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/focus/FocusDetector.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/focus/InputFocusService.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/focus/ReflectionWalker.java`

### Compatibility detectors

- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/VanillaTextFieldDetector.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi1TextFieldDetector.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi2TextFieldDetector.java`

### Tests

- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNHMetadataTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfoTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridgeTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/focus/InputFocusServiceTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi1TextFieldDetectorTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi2TextFieldDetectorTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/client/ClientEventHandlerTest.java`

### Tracking docs

- Modify: `log.md`
- Modify: `ToDOLIST.md`
- Modify: `context.md`

## Key Implementation Decisions

- Use `modId = inputmethodblockergtnh`
- Use `modName = InputMethodBlocker-GTNH`
- Use package root `com.github.skystardust.inputmethodblockergtnh`
- Keep server proxy inert; all IME logic is client-only
- Keep MUI support generic by inspecting object graphs and common focus accessors instead of naming specific mods
- Wrap legacy native calls behind `NativeImeBindings` so tests never call `System.load` or JNI directly

### Task 1: Bootstrap the GTNH Project Skeleton

**Files:**
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `settings.gradle`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNH.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/CommonProxy.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ClientProxy.java`
- Create: `src/main/resources/mcmod.info`
- Create: `src/main/resources/assets/inputmethodblockergtnh/lang/en_US.lang`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNHMetadataTest.java`
- Copy: `src/main/resources/InputMethodBlocker-Natives-x86.dll`
- Copy: `src/main/resources/InputMethodBlocker-Natives-x64.dll`

- [ ] **Step 1: Create the Gradle bootstrap files and source folders**

```groovy
// build.gradle
plugins {
    id 'com.github.ElytraServers.elytra-conventions' version 'v1.1.2'
    id 'com.gtnewhorizons.gtnhconvention'
}

afterEvaluate {
    if (project.hasProperty('modVersion')) {
        project.version = project.property('modVersion')
    }
}

minecraft {
    extraRunJvmArguments.add("-Xmx4G")
    extraRunJvmArguments.add("-Xms4G")
}

test {
    useJUnitPlatform()
}
```

```properties
# gradle.properties
gtnh.settings.blowdryerTag = 0.2.2
modName = InputMethodBlocker-GTNH
modVersion = 0.1.0
modId = inputmethodblockergtnh
modGroup = com.github.skystardust.inputmethodblockergtnh
useModGroupForPublishing = true
autoUpdateBuildScript = true
minecraftVersion = 1.7.10
forgeVersion = 10.13.4.1614
channel = stable
mappingsVersion = 12
remoteMappings = https\://raw.githubusercontent.com/MinecraftForge/FML/1.7.10/conf/
developmentEnvironmentUserName = Developer
enableModernJavaSyntax = true
enableGenericInjection = true
generateGradleTokenClass = com.github.skystardust.inputmethodblockergtnh.Tags
gradleTokenVersion = VERSION
apiPackage =
accessTransformersFile =
usesMixins = false
forceEnableMixins = false
usesShadowedDependencies = false
includeWellKnownRepositories = true
usesMavenPublishing = false
disableSpotless = false
disableCheckstyle = true
```

```groovy
// settings.gradle
pluginManagement {
    repositories {
        maven {
            name "GTNH Maven"
            url "https://nexus.gtnewhorizons.com/repository/public/"
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        maven {
            name "Jitpack"
            url "https://jitpack.io"
        }
        mavenLocal()
    }
}

plugins {
    id 'com.gtnewhorizons.gtnhsettingsconvention' version '1.0.50'
}
```

- [ ] **Step 2: Verify the Gradle wrapper and conventions boot correctly**

Run:
```powershell
./gradlew.bat help
```

Expected: exit code `0` and Gradle lists standard GTNH tasks such as `runClient`, `compileJava`, and `test`.

- [ ] **Step 3: Write the failing metadata test**

```java
package com.github.skystardust.inputmethodblockergtnh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

class InputMethodBlockerGTNHMetadataTest {

    @Test
    void exposesStableModMetadataAndShipsNativeLibraries() {
        assertEquals("inputmethodblockergtnh", InputMethodBlockerGTNH.MODID);
        assertEquals("InputMethodBlocker-GTNH", InputMethodBlockerGTNH.MOD_NAME);

        try (InputStream x86 = getClass().getClassLoader()
            .getResourceAsStream("InputMethodBlocker-Natives-x86.dll");
            InputStream x64 = getClass().getClassLoader()
                .getResourceAsStream("InputMethodBlocker-Natives-x64.dll")) {
            assertTrue(x86 != null, "x86 native library must be packaged");
            assertTrue(x64 != null, "x64 native library must be packaged");
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
```

- [ ] **Step 4: Run the metadata test to verify it fails**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNHMetadataTest
```

Expected: FAIL because `InputMethodBlockerGTNH` and packaged DLL resources do not exist yet.

- [ ] **Step 5: Add the minimal mod entry classes and resources**

```java
package com.github.skystardust.inputmethodblockergtnh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = InputMethodBlockerGTNH.MODID,
    version = InputMethodBlockerGTNH.VERSION,
    name = InputMethodBlockerGTNH.MOD_NAME,
    acceptedMinecraftVersions = "[1.7.10]")
public class InputMethodBlockerGTNH {

    public static final String MODID = "inputmethodblockergtnh";
    public static final String MOD_NAME = "InputMethodBlocker-GTNH";
    public static final String VERSION = Tags.VERSION;

    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.github.skystardust.inputmethodblockergtnh.ClientProxy",
        serverSide = "com.github.skystardust.inputmethodblockergtnh.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
```

```java
package com.github.skystardust.inputmethodblockergtnh;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {}
}
```

```java
package com.github.skystardust.inputmethodblockergtnh;

public class ClientProxy extends CommonProxy {}
```

```json
[
  {
    "modid": "inputmethodblockergtnh",
    "name": "InputMethodBlocker-GTNH",
    "version": "${version}",
    "mcversion": "1.7.10",
    "description": "IME switching support for GTNH text input UIs on Java 17-25."
  }
]
```

```properties
# assets/inputmethodblockergtnh/lang/en_US.lang
inputmethodblockergtnh.name=InputMethodBlocker-GTNH
inputmethodblockergtnh.description=IME switching support for GTNH text input UIs on Java 17-25.
```

Copy the DLLs from the legacy mod:

```powershell
Copy-Item ..\InputMethodBlocker-master\1.7.x\src\main\resources\InputMethodBlocker-Natives-x86.dll src\main\resources\
Copy-Item ..\InputMethodBlocker-master\1.7.x\src\main\resources\InputMethodBlocker-Natives-x64.dll src\main\resources\
```

- [ ] **Step 6: Run the metadata test and compile to verify the scaffold**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNHMetadataTest
./gradlew.bat compileJava
```

Expected: the metadata test passes and `compileJava` succeeds.

- [ ] **Step 7: Commit the scaffold**

```powershell
git init
git add build.gradle gradle.properties settings.gradle src/main/java src/main/resources src/test/java
git commit -m "chore: bootstrap InputMethodBlocker-GTNH project"
```

### Task 2: Implement the Safe Windows IME Bridge

**Files:**
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/ImeBridge.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/DisabledImeBridge.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfo.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/NativeLibraryExtractor.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/NativeImeBindings.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridge.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/PlatformInfoTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/ime/WindowsImeBridgeTest.java`

- [ ] **Step 1: Write failing tests for platform detection and bridge state transitions**

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class PlatformInfoTest {

    @Test
    void identifiesWindows64BitRuntime() {
        Properties properties = new Properties();
        properties.setProperty("os.name", "Windows 11");
        properties.setProperty("os.arch", "amd64");

        PlatformInfo info = PlatformInfo.from(properties);

        assertTrue(info.isWindows());
        assertEquals("InputMethodBlocker-Natives-x64.dll", info.nativeResourceName());
    }

    @Test
    void rejectsNonWindowsRuntimes() {
        Properties properties = new Properties();
        properties.setProperty("os.name", "Linux");
        properties.setProperty("os.arch", "amd64");

        PlatformInfo info = PlatformInfo.from(properties);

        assertTrue(!info.isWindows());
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
    void becomesAvailableAfterSuccessfulLoadAndDebouncesDuplicateState() {
        RecordingBindings bindings = new RecordingBindings();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.windows64(),
            resourceName -> new File(resourceName),
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);
        bridge.setImeActive(true);
        bridge.setImeActive(false);

        assertTrue(bridge.isAvailable());
        assertEquals(1, bindings.activeCalls);
        assertEquals(1, bindings.inactiveCalls);
    }

    @Test
    void staysDisabledOutsideWindows() {
        RecordingBindings bindings = new RecordingBindings();
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.nonWindows("Linux", "amd64"),
            resourceName -> new File(resourceName),
            bindings);

        bridge.initialize();
        bridge.setImeActive(true);

        assertFalse(bridge.isAvailable());
        assertEquals(0, bindings.activeCalls);
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

- [ ] **Step 2: Run the IME tests to verify they fail**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.ime.PlatformInfoTest --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest
```

Expected: FAIL because the IME bridge classes do not exist yet.

- [ ] **Step 3: Implement the bridge interfaces and the native extractor**

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

public interface ImeBridge {

    void initialize();

    boolean isAvailable();

    void setImeActive(boolean active);
}
```

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

public final class PlatformInfo {

    private final String osName;
    private final String osArch;
    private final boolean windows;

    private PlatformInfo(String osName, String osArch, boolean windows) {
        this.osName = osName;
        this.osArch = osArch;
        this.windows = windows;
    }

    public static PlatformInfo from(java.util.Properties properties) {
        String osName = properties.getProperty("os.name", "");
        String osArch = properties.getProperty("os.arch", "");
        return new PlatformInfo(osName, osArch, osName.toLowerCase().contains("win"));
    }

    public static PlatformInfo windows64() {
        return new PlatformInfo("Windows", "amd64", true);
    }

    public static PlatformInfo nonWindows(String osName, String osArch) {
        return new PlatformInfo(osName, osArch, false);
    }

    public boolean isWindows() {
        return windows;
    }

    public String nativeResourceName() {
        return osArch.contains("64") ? "InputMethodBlocker-Natives-x64.dll" : "InputMethodBlocker-Natives-x86.dll";
    }
}
```

```java
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
```

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

public interface NativeImeBindings {

    void activeInputMethod(String windowName);

    void inactiveInputMethod(String windowName);
}
```

- [ ] **Step 4: Implement the safe Windows bridge**

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

import com.github.skystardust.inputmethodblockergtnh.InputMethodBlockerGTNH;

public class WindowsImeBridge implements ImeBridge {

    private final PlatformInfo platformInfo;
    private final Function<String, File> extractor;
    private final NativeImeBindings bindings;

    private boolean initialized;
    private boolean available;
    private Boolean currentActive;

    public WindowsImeBridge(PlatformInfo platformInfo, Function<String, File> extractor, NativeImeBindings bindings) {
        this.platformInfo = Objects.requireNonNull(platformInfo);
        this.extractor = Objects.requireNonNull(extractor);
        this.bindings = Objects.requireNonNull(bindings);
    }

    @Override
    public void initialize() {
        if (initialized) return;
        initialized = true;
        if (!platformInfo.isWindows()) {
            InputMethodBlockerGTNH.LOG.info("IME bridge disabled: non-Windows platform");
            return;
        }
        try {
            File library = extractor.apply(platformInfo.nativeResourceName());
            System.load(library.getAbsolutePath());
            available = true;
        } catch (Throwable t) {
            InputMethodBlockerGTNH.LOG.warn("IME bridge unavailable", t);
            available = false;
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setImeActive(boolean active) {
        if (!available || Objects.equals(currentActive, active)) return;
        currentActive = active;
        if (active) {
            bindings.activeInputMethod("");
        } else {
            bindings.inactiveInputMethod("");
        }
    }
}
```

```java
package com.github.skystardust.inputmethodblockergtnh.ime;

public class DisabledImeBridge implements ImeBridge {

    @Override
    public void initialize() {}

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void setImeActive(boolean active) {}
}
```

- [ ] **Step 5: Run the IME tests to verify they pass**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.ime.PlatformInfoTest --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest
```

Expected: PASS.

- [ ] **Step 6: Commit the IME bridge**

```powershell
git add src/main/java/com/github/skystardust/inputmethodblockergtnh/ime src/test/java/com/github/skystardust/inputmethodblockergtnh/ime
git commit -m "feat: add safe Windows IME bridge"
```

### Task 3: Add Focus Aggregation and Vanilla GUI Detection

**Files:**
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/focus/FocusDetector.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/focus/InputFocusService.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/focus/ReflectionWalker.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/VanillaTextFieldDetector.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/focus/InputFocusServiceTest.java`

- [ ] **Step 1: Write failing tests for focus aggregation**

```java
package com.github.skystardust.inputmethodblockergtnh.focus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class InputFocusServiceTest {

    @Test
    void reportsFocusedWhenAnyDetectorMatches() {
        InputFocusService service = new InputFocusService(
            Arrays.asList(screen -> false, screen -> true, screen -> false));

        assertTrue(service.shouldEnableIme(new Object()));
    }

    @Test
    void onlyPublishesStateChangeWhenFocusFlips() {
        AtomicInteger changes = new AtomicInteger();
        InputFocusService service = new InputFocusService(Arrays.asList(screen -> true));

        service.update(new Object(), active -> changes.incrementAndGet());
        service.update(new Object(), active -> changes.incrementAndGet());
        service.reset(active -> changes.incrementAndGet());

        assertEquals(2, changes.get());
    }
}
```

- [ ] **Step 2: Run the focus test to verify it fails**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.focus.InputFocusServiceTest
```

Expected: FAIL because the focus service classes do not exist yet.

- [ ] **Step 3: Implement the focus interfaces and reflection walker**

```java
package com.github.skystardust.inputmethodblockergtnh.focus;

@FunctionalInterface
public interface FocusDetector {

    boolean hasFocusedTextInput(Object screen);
}
```

```java
package com.github.skystardust.inputmethodblockergtnh.focus;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ReflectionWalker {

    public boolean contains(Object root, Predicate<Object> predicate) {
        if (root == null) return false;

        ArrayDeque<Object> queue = new ArrayDeque<>();
        Set<Object> visited = new HashSet<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Object next = queue.removeFirst();
            if (next == null || visited.contains(next)) continue;
            visited.add(next);
            if (predicate.test(next)) return true;

            for (Field field : next.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    queue.add(field.get(next));
                } catch (IllegalAccessException ignored) {}
            }
        }
        return false;
    }
}
```

```java
package com.github.skystardust.inputmethodblockergtnh.focus;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class InputFocusService {

    private final List<FocusDetector> detectors;
    private Boolean lastState;

    public InputFocusService(List<FocusDetector> detectors) {
        this.detectors = Objects.requireNonNull(detectors);
    }

    public boolean shouldEnableIme(Object screen) {
        for (FocusDetector detector : detectors) {
            if (detector.hasFocusedTextInput(screen)) return true;
        }
        return false;
    }

    public void update(Object screen, Consumer<Boolean> listener) {
        boolean current = shouldEnableIme(screen);
        if (!Objects.equals(lastState, current)) {
            lastState = current;
            listener.accept(current);
        }
    }

    public void reset(Consumer<Boolean> listener) {
        if (!Objects.equals(lastState, false)) {
            lastState = false;
            listener.accept(false);
        }
    }
}
```

- [ ] **Step 4: Implement the vanilla detector**

```java
package com.github.skystardust.inputmethodblockergtnh.compat;

import java.lang.reflect.Method;

import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.ReflectionWalker;

public class VanillaTextFieldDetector implements FocusDetector {

    private final ReflectionWalker walker = new ReflectionWalker();

    @Override
    public boolean hasFocusedTextInput(Object screen) {
        return walker.contains(screen, candidate -> {
            if (candidate == null) return false;
            if (!"net.minecraft.client.gui.GuiTextField".equals(candidate.getClass().getName())) return false;
            try {
                Method method = candidate.getClass().getMethod("isFocused");
                return Boolean.TRUE.equals(method.invoke(candidate));
            } catch (ReflectiveOperationException e) {
                return false;
            }
        });
    }
}
```

- [ ] **Step 5: Run the focus test and compileJava**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.focus.InputFocusServiceTest
./gradlew.bat compileJava
```

Expected: PASS for the focus test and successful compilation of the new detector classes.

- [ ] **Step 6: Commit the focus layer**

```powershell
git add src/main/java/com/github/skystardust/inputmethodblockergtnh/focus src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/VanillaTextFieldDetector.java src/test/java/com/github/skystardust/inputmethodblockergtnh/focus
git commit -m "feat: add focus aggregation and vanilla gui detection"
```

### Task 4: Add Generic MUI1 and MUI2 Text Field Detection

**Files:**
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi1TextFieldDetector.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi2TextFieldDetector.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi1TextFieldDetectorTest.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/compat/ModularUi2TextFieldDetectorTest.java`

- [ ] **Step 1: Write failing detector tests using fake MUI widget trees**

```java
package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModularUi1TextFieldDetectorTest {

    @Test
    void detectsFocusedMui1TextFieldAnywhereInTree() {
        FakeMUI1Window screen = new FakeMUI1Window(new FakeMUI1Node(new com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget(true)));

        assertTrue(new ModularUi1TextFieldDetector().hasFocusedTextInput(screen));
    }

    @Test
    void ignoresUnfocusedMui1TextFields() {
        FakeMUI1Window screen = new FakeMUI1Window(new FakeMUI1Node(new com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget(false)));

        assertFalse(new ModularUi1TextFieldDetector().hasFocusedTextInput(screen));
    }

    private static final class FakeMUI1Window {
        private final Object root;

        private FakeMUI1Window(Object root) {
            this.root = root;
        }
    }

    private static final class FakeMUI1Node {
        private final Object child;

        private FakeMUI1Node(Object child) {
            this.child = child;
        }
    }
}
```

```java
package com.gtnewhorizons.modularui.common.widget.textfield;

public class TextFieldWidget {
    private final boolean focused;

    public TextFieldWidget(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
```

```java
package com.cleanroommc.modularui.widgets.textfield;

public class TextFieldWidget {
    private final boolean focused;

    public TextFieldWidget(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }
}
```

Add the analogous MUI2 test:

```java
package com.github.skystardust.inputmethodblockergtnh.compat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModularUi2TextFieldDetectorTest {

    @Test
    void detectsFocusedMui2TextFieldAnywhereInTree() {
        Object screen = new Object() {
            final Object panel = new Object() {
                final Object field = new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget(true);
            };
        };

        assertTrue(new ModularUi2TextFieldDetector().hasFocusedTextInput(screen));
    }
}
```

- [ ] **Step 2: Run the MUI detector tests to verify they fail**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.ModularUi1TextFieldDetectorTest --tests com.github.skystardust.inputmethodblockergtnh.compat.ModularUi2TextFieldDetectorTest
```

Expected: FAIL because the detector classes do not exist yet.

- [ ] **Step 3: Implement the MUI1 and MUI2 detectors**

```java
package com.github.skystardust.inputmethodblockergtnh.compat;

import java.lang.reflect.Method;

import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.ReflectionWalker;

public class ModularUi1TextFieldDetector implements FocusDetector {

    private final ReflectionWalker walker = new ReflectionWalker();

    @Override
    public boolean hasFocusedTextInput(Object screen) {
        return walker.contains(screen, candidate -> isFocused(candidate, "com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget"));
    }

    private boolean isFocused(Object candidate, String className) {
        if (candidate == null || !className.equals(candidate.getClass().getName())) return false;
        try {
            Method method = candidate.getClass().getMethod("isFocused");
            return Boolean.TRUE.equals(method.invoke(candidate));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
```

```java
package com.github.skystardust.inputmethodblockergtnh.compat;

import java.lang.reflect.Method;

import com.github.skystardust.inputmethodblockergtnh.focus.FocusDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.ReflectionWalker;

public class ModularUi2TextFieldDetector implements FocusDetector {

    private final ReflectionWalker walker = new ReflectionWalker();

    @Override
    public boolean hasFocusedTextInput(Object screen) {
        return walker.contains(screen, candidate -> isFocused(candidate, "com.cleanroommc.modularui.widgets.textfield.TextFieldWidget"));
    }

    private boolean isFocused(Object candidate, String className) {
        if (candidate == null || !className.equals(candidate.getClass().getName())) return false;
        try {
            Method method = candidate.getClass().getMethod("isFocused");
            return Boolean.TRUE.equals(method.invoke(candidate));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
```

- [ ] **Step 4: Run the MUI tests to verify they pass**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.ModularUi1TextFieldDetectorTest --tests com.github.skystardust.inputmethodblockergtnh.compat.ModularUi2TextFieldDetectorTest
```

Expected: PASS.

- [ ] **Step 5: Run compileJava to confirm the detectors integrate with main sources**

Run:
```powershell
./gradlew.bat compileJava
```

Expected: PASS.

- [ ] **Step 6: Commit the MUI detectors**

```powershell
git add src/main/java/com/github/skystardust/inputmethodblockergtnh/compat src/test/java/com/github/skystardust/inputmethodblockergtnh/compat src/test/java/com/gtnewhorizons src/test/java/com/cleanroommc
git commit -m "feat: add generic mui text field detectors"
```

### Task 5: Wire Client Lifecycle, Add Transition Guards, and Verify the Whole Feature

**Files:**
- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/ClientProxy.java`
- Create: `src/main/java/com/github/skystardust/inputmethodblockergtnh/client/ClientEventHandler.java`
- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/CommonProxy.java`
- Modify: `src/main/java/com/github/skystardust/inputmethodblockergtnh/InputMethodBlockerGTNH.java`
- Create: `src/test/java/com/github/skystardust/inputmethodblockergtnh/client/ClientEventHandlerTest.java`
- Modify: `log.md`
- Modify: `ToDOLIST.md`
- Modify: `context.md`

- [ ] **Step 1: Write the failing client event handler test**

```java
package com.github.skystardust.inputmethodblockergtnh.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.skystardust.inputmethodblockergtnh.focus.InputFocusService;
import com.github.skystardust.inputmethodblockergtnh.ime.ImeBridge;

class ClientEventHandlerTest {

    @Test
    void togglesBridgeOnlyWhenFocusChanges() {
        RecordingBridge bridge = new RecordingBridge();
        ClientEventHandler handler = new ClientEventHandler(
            bridge,
            new InputFocusService(Arrays.asList(screen -> Boolean.TRUE.equals(screen))));

        handler.onScreenTick(Boolean.TRUE);
        handler.onScreenTick(Boolean.TRUE);
        handler.onScreenTick(Boolean.FALSE);

        assertEquals(2, bridge.calls);
    }

    private static final class RecordingBridge implements ImeBridge {
        private int calls;

        @Override
        public void initialize() {}

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public void setImeActive(boolean active) {
            calls++;
        }
    }
}
```

- [ ] **Step 2: Run the client event handler test to verify it fails**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.client.ClientEventHandlerTest
```

Expected: FAIL because the event handler does not exist yet.

- [ ] **Step 3: Implement the client event handler and proxy wiring**

```java
package com.github.skystardust.inputmethodblockergtnh.client;

import com.github.skystardust.inputmethodblockergtnh.focus.InputFocusService;
import com.github.skystardust.inputmethodblockergtnh.ime.ImeBridge;

public class ClientEventHandler {

    private final ImeBridge bridge;
    private final InputFocusService focusService;

    public ClientEventHandler(ImeBridge bridge, InputFocusService focusService) {
        this.bridge = bridge;
        this.focusService = focusService;
    }

    public void onScreenTick(Object currentScreen) {
        if (currentScreen == null) {
            focusService.reset(bridge::setImeActive);
            return;
        }
        focusService.update(currentScreen, bridge::setImeActive);
    }
}
```

```java
package com.github.skystardust.inputmethodblockergtnh;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.TickEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import com.github.skystardust.inputmethodblockergtnh.client.ClientEventHandler;
import com.github.skystardust.inputmethodblockergtnh.compat.ModularUi1TextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.compat.ModularUi2TextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.compat.VanillaTextFieldDetector;
import com.github.skystardust.inputmethodblockergtnh.focus.InputFocusService;
import com.github.skystardust.inputmethodblockergtnh.ime.NativeImeBindings;
import com.github.skystardust.inputmethodblockergtnh.ime.NativeLibraryExtractor;
import com.github.skystardust.inputmethodblockergtnh.ime.PlatformInfo;
import com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridge;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        WindowsImeBridge bridge = new WindowsImeBridge(
            PlatformInfo.from(System.getProperties()),
            resource -> new NativeLibraryExtractor().extract(resource),
            new NativeImeBindings() {
                @Override
                public void activeInputMethod(String windowName) {
                    NativeMethods.activeInputMethod(windowName);
                }

                @Override
                public void inactiveInputMethod(String windowName) {
                    NativeMethods.inactiveInputMethod(windowName);
                }
            });
        bridge.initialize();

        ClientEventHandler handler = new ClientEventHandler(
            bridge,
            new InputFocusService(Arrays.asList(
                new VanillaTextFieldDetector(),
                new ModularUi1TextFieldDetector(),
                new ModularUi2TextFieldDetector())));

        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onClientTick(TickEvent.ClientTickEvent tickEvent) {
                if (tickEvent.phase == TickEvent.Phase.END) {
                    handler.onScreenTick(Minecraft.getMinecraft().currentScreen);
                }
            }
        });
    }
}
```

```java
package com.github.skystardust.inputmethodblockergtnh;

final class NativeMethods {

    private NativeMethods() {}

    static native void activeInputMethod(String windowName);

    static native void inactiveInputMethod(String windowName);
}
```

- [ ] **Step 4: Run the targeted client test and then the full automated suite**

Run:
```powershell
./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.client.ClientEventHandlerTest
./gradlew.bat test
./gradlew.bat compileJava
./gradlew.bat spotlessCheck
```

Expected: all tests pass, `compileJava` succeeds, and `spotlessCheck` reports no formatting violations.

- [ ] **Step 5: Perform the manual in-game verification pass**

Run:
```powershell
./gradlew.bat runClient
```

Expected manual checks:
- Chat screen enables IME when the text box is focused
- Closing chat disables IME
- Sign edit and anvil rename screens enable IME
- At least one MUI1 text box in GT5 or another GTNH mod enables IME
- At least one MUI2 text box in GT5 or another GTNH mod enables IME
- Non-text MUI screens do not spuriously enable IME

- [ ] **Step 6: Refresh the project tracking docs**

```markdown
<!-- log.md -->
## 2026-04-19：实现计划编写完成

### 已完成
- 编写 `docs/superpowers/plans/2026-04-19-inputmethodblocker-gtnh-implementation.md`
- 将后续实现拆分为工程骨架、IME bridge、原版探测、MUI1/MUI2 探测、客户端联调五个任务
```

```markdown
<!-- ToDOLIST.md -->
## 当前计划
- [ ] 按实现计划执行 Task 1-Task 5
```

```markdown
<!-- context.md -->
## 架构说明
- 已存在经确认的实现计划文档：`docs/superpowers/plans/2026-04-19-inputmethodblocker-gtnh-implementation.md`
```

- [ ] **Step 7: Commit the wired feature**

```powershell
git add src/main/java src/test/java log.md ToDOLIST.md context.md docs/superpowers/plans/2026-04-19-inputmethodblocker-gtnh-implementation.md
git commit -m "feat: wire ime switching through generic focus detectors"
```

## Final Verification Checklist

- [ ] `./gradlew.bat test`
- [ ] `./gradlew.bat compileJava`
- [ ] `./gradlew.bat spotlessCheck`
- [ ] `./gradlew.bat runClient`
- [ ] Chat, sign, anvil, MUI1, and MUI2 text input tested manually on Windows
- [ ] `log.md`, `ToDOLIST.md`, and `context.md` updated in Chinese
