# 开发日志

## 2026-04-20：加入 IME 诊断日志以定位 Java 层还是 native 层失效

### 已完成
- 为 `InputFocusService` 新增 `findMatchingDetector(Object)`，用于诊断当前 GUI 由哪个 detector 命中
- 为 `ClientEventHandler` 增加高信号日志：在 screen、命中 detector、期望 IME 状态变化时输出 `screen=... detector=... desiredImeActive=... bridgeAvailable=...`
- 为 `WindowsImeBridge` 增加诊断日志：
  - 初始化成功时记录已加载 DLL 并已执行初始 `inactive`
  - native `activeInputMethod` / `inactiveInputMethod` 在前几次调用及每 100 次调用时输出计数
  - bridge 不可用时首次收到状态变更请求会输出警告
- 重新通过 `./gradlew.bat test`、`./gradlew.bat compileJava` 与 `./gradlew.bat assemble`

### 遇到的问题
- **仅靠 Java 层逻辑推断已经不够**：实机现象表明可能是焦点层、bridge 层或 DLL/native 层中的任意一层失效，需要日志把链路切开
- **并发执行 `test` 与 `assemble` 会污染同一 Gradle 工作目录状态**：一次并行构建出现了编译阶段读取到不完整状态的假失败，后续改回串行验证

### 已做决定
- 下一步以 `latest.log` 中的 `inputmethodblockergtnh` 诊断日志为主线继续定位，不再盲改 Java 状态机
- 构建验证保持串行执行，避免再引入 Gradle 并发假失败

---

## 2026-04-20：修复非输入状态下未持续压制 IME 的问题

### 已完成
- 根据实机现象确认第二个根因：当前实现对 `false` 状态做了双重去抖，用户手动按 `Shift` 切回中文后，没有新的 native `inactive` 再次压回去
- 新增回归测试：
  - `InputFocusServiceTest.republishesInactiveWhileNoDetectorMatches()`
  - `WindowsImeBridgeTest.reappliesInactiveEvenWhenAlreadyInactive()`
- 调整 `InputFocusService`：
  - `true` 仍然按状态翻转去抖
  - `false` 改为每次 `update/reset` 都重新发布，允许上层持续压制 IME
- 调整 `WindowsImeBridge.setImeActive(boolean)`：
  - `true` 继续去抖
  - `false` 不再因 `currentActive == false` 被跳过，而是允许重复调用 `inactiveInputMethod("")`
- 同步更新 `InputFocusServiceTest` 与 `WindowsImeBridgeTest` 的旧断言，使其符合新的运行期压制语义
- 重新通过 `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.focus.InputFocusServiceTest --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest`、`./gradlew.bat test` 与 `./gradlew.bat assemble`

### 遇到的问题
- **当前架构对 inactive 状态做了双重消抖**：`InputFocusService` 和 `WindowsImeBridge` 都只在状态变化时下发 `false`，导致运行中手动切回输入法后没有新的 native 压制
- **修复后部分旧测试期望失效**：旧测试默认 `false` 与 `reset` 也是边沿触发，需要同步到“持续压制 inactive”的新语义

### 已做决定
- 维持 `active` 的边沿触发，避免在文本输入状态下无意义重复调 native
- 对 `inactive` 则改为持续压制，以优先满足“阻止非输入状态下用 `Shift` 切换输入法”的目标

---

## 2026-04-20：修复 Windows IME bridge 未同步初始 inactive 状态

### 已完成
- 对照旧版 `InputMethodBlocker-master/1.7.x` 确认行为差异：旧模组在 DLL 成功加载后会立即调用一次 `NativeUtils.inactiveInputMethod("")`
- 为 `WindowsImeBridge` 新增初始化回归测试，验证成功加载后必须先把 native IME 状态压到 inactive
- 修复 `WindowsImeBridge.initialize()`：在成功 `load` DLL 后立刻调用 `bindings.inactiveInputMethod("")`，并同步设置 `currentActive = false`
- 同步更新 `WindowsImeBridgeTest` 的计数期望，使其符合旧模组“初始化先 inactive，再按焦点切换 active/inactive”的语义
- 重新通过 `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.ime.WindowsImeBridgeTest`、`./gradlew.bat test` 与 `./gradlew.bat assemble`

### 遇到的问题
- **新桥接层缺失了旧模组的一步关键初始化同步**：虽然 DLL 能加载，但 native 侧初始输入法状态没有在加载后立即压回 inactive
- **修复后旧测试期望失效**：原测试默认“初始化阶段不触发 native 调用”，需要改为和旧模组语义一致

### 已做决定
- `WindowsImeBridge` 保持与旧模组一致的初始化顺序：成功加载 DLL 后立即执行一次 `inactiveInputMethod("")`
- 当前先只修复已验证的初始化根因，不把“运行中重复强制 inactive”一并打包进同一次修复

---

## 2026-04-20：将非原版检测统一收敛为可选兼容层

### 已完成
- 调整 `ClientProxy` 的 detector 装配逻辑：`VanillaTextFieldDetector` 常驻，其他 detector 全部按模组或类是否存在再注册
- 新增 `ClientProxyTest`，覆盖“无兼容模组时仅保留原版 detector”与“命中对应模组/类时才加入兼容 detector”两种场景
- `ModSearchTextFieldDetector` 改为仅在检测到 `angelica`、`nei` / `NotEnoughItems`、`serverutilities` 之一已加载时注册
- `AeTerminalTextFieldDetector` 改为仅在检测到 `appliedenergistics2` 或 `ae2thing` 已加载时注册
- `ModularUi1TextFieldDetector` 与 `ModularUi2TextFieldDetector` 改为仅在对应类可用时注册
- 强化兼容类存在性检测，`Class.forName` 改为不初始化加载，并额外容错 `LinkageError`
- 重新通过 `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.ClientProxyTest`、`./gradlew.bat test` 与 `./gradlew.bat assemble`

### 遇到的问题
- **之前虽然非原版 detector 不会因为缺模组直接崩溃，但它们仍会常驻在焦点链里**：这在架构语义上不符合“可选兼容层”的边界
- **可选类探测不能只捕获 `ClassNotFoundException`**：如果类存在但其依赖缺失，仍可能抛出 `LinkageError`

### 已做决定
- 除原版白名单外，所有第三方 GUI 支持都视为 compat 层
- compat 层是否启用以“模组已加载”或“类可用”为准，不再默认常驻

---

## 2026-04-20：为 Angelica、NEI、ServerUtilities 添加搜索框与输入框白名单

### 已完成
- 新增 `ModSearchTextFieldDetector`，负责 Angelica、NEI、ServerUtilities 的普通模组搜索框与输入框白名单检测
- 将 `ModSearchTextFieldDetector` 接入 `ClientProxy`
- 扩展 `WhitelistedReflectiveTextFieldDetector`：
  - 支持静态字段白名单，覆盖 NEI 的 `LayoutManager.searchField`、`LayoutManager.quantity`、`GuiRecipe.searchField`
  - 支持按继承链匹配文本框类型，兼容匿名子类与派生类
  - 支持多个焦点方法名，兼容 `isFocused()` 与 `focused()`
  - 支持同一白名单项下多个字段“任一聚焦即命中”
- 首批 Angelica 白名单已加入：
  - `com.gtnewhorizons.angelica.client.gui.FontConfigScreen` -> `searchBox`、`testArea`
  - `me.flashyreese.mods.reeses_sodium_options.client.gui.ReeseSodiumVideoOptionsScreen` -> `searchTextField`
- 首批 NEI 白名单已加入：
  - `net.minecraft.client.gui.inventory.GuiContainer` -> `codechicken.nei.LayoutManager.searchField`、`quantity`
  - `codechicken.nei.recipe.GuiRecipe` -> `codechicken.nei.recipe.GuiRecipe.searchField`
  - `codechicken.nei.GuiPotionCreator` -> `durationField`
- 首批 ServerUtilities 白名单已加入：
  - `serverutils.lib.gui.misc.GuiButtonListBase` -> `searchBox`
  - `serverutils.lib.gui.misc.GuiSelectItemStack` -> `searchBox`
  - `serverutils.client.gui.teams.GuiCreateTeam` -> `textBoxId`
  - `serverutils.client.gui.ranks.GuiAddRank` -> `textBoxId`
  - `serverutils.lib.gui.misc.GuiEditConfigValue` -> `textBox`
- 明确不加入 `serverutils.invsee.GuiInvseeContainer.textField`，因为它只是显示文本，不是可输入控件
- 新增 `ModSearchTextFieldDetectorTest`
- 重新通过 `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.ModSearchTextFieldDetectorTest`、`./gradlew.bat test` 与 `./gradlew.bat assemble`

### 遇到的问题
- **NEI 的一部分输入框不是当前 screen 的实例字段**：例如 `LayoutManager.searchField` 与 `quantity` 属于静态控件，不能只靠 screen 实例字段检测
- **NEI 与 ServerUtilities 常用匿名子类或派生文本框**：如果只按精确类名匹配，会漏掉 `RecipeSearchField`、匿名 `TextBox` 等实际输入控件

### 已做决定
- 普通模组兼容单独归到 `ModSearchTextFieldDetector`
- 继续坚持纯白名单路线，不恢复任意对象图扫描
- 对于深层嵌套、无法直接通过白名单字段命中的 GUI，后续按具体界面继续补显式路径或专用 detector

---

## 2026-04-19：为 AE2 与 AE2Things 终端搜索框添加白名单

### 已完成
- 新增 `AeTerminalTextFieldDetector`，专门负责 AE 系终端搜索框的白名单检测
- 扩展 `WhitelistedReflectiveTextFieldDetector`，使其可同时支持多个文本框类名
- 将 `AeTerminalTextFieldDetector` 接入 `ClientProxy`，并继续限制为仅在游戏内启用
- 首批 AE2 白名单已加入：
  - `appeng.client.gui.implementations.GuiMEMonitorable` -> `searchField`
  - `appeng.client.gui.implementations.GuiInterfaceTerminal` -> `searchFieldInputs`、`searchFieldOutputs`、`searchFieldNames`
- 首批 AE2Things 白名单已加入：
  - `com.asdflj.ae2thing.client.gui.GuiMonitor` -> `searchField`
  - `com.asdflj.ae2thing.client.gui.GuiBaseInterfaceWireless` -> `searchFieldInputs`、`searchFieldOutputs`、`searchFieldNames`
  - `com.asdflj.ae2thing.client.gui.GuiWirelessConnectorTerminal` -> `searchField`
  - `com.asdflj.ae2thing.client.gui.GuiWirelessDistributor` -> `searchField`
- 新增 `AeTerminalTextFieldDetectorTest`
- 重新通过 `./gradlew.bat test` 与 `./gradlew.bat assemble`

### 遇到的问题
- **AE2 与 AE2Things 的终端搜索框并不只使用一种文本框类**：AE2 主要使用 `MEGuiTextField`，AE2Things 还额外使用 `THGuiTextField`

### 已做决定
- AE 系终端兼容单独归到 `AeTerminalTextFieldDetector`
- 后续继续补 AE 终端时，优先沿用这个 detector 的白名单 map，而不是拆回原版或 MUI detector

---

## 2026-04-19：移除遗留通用扫描代码

### 已完成
- 新增 `FocusArchitectureGuardTest`，约束运行产物中不再包含 `ReflectionWalker` 这一通用反射扫描类
- 删除未再参与运行路径的 `ReflectionWalker` 与其旧测试 `ReflectionWalkerTest`
- 保留现有白名单实现：`VanillaTextFieldDetector`、`ModularUi1TextFieldDetector`、`ModularUi2TextFieldDetector`
- 重新通过 `./gradlew.bat test` 与 `./gradlew.bat assemble`，确认移除遗留代码后构建与测试均正常

### 遇到的问题
- **代码库中仍残留未被运行路径使用的通用扫描实现**：虽然当前不再生效，但会给后续维护带来误导，必须从产物中移除

### 已做决定
- 后续不再保留任何“备用通用扫描”实现
- 所有 GUI 输入焦点检测都必须通过显式白名单扩展

---

## 2026-04-19：将 MUI1 和 MUI2 检测改为白名单

### 已完成
- 将 `ModularUi1TextFieldDetector` 与 `ModularUi2TextFieldDetector` 从通用反射扫描改为白名单 screen/field 检测
- 新增共享 helper `WhitelistedReflectiveTextFieldDetector`，用于按“界面类名 + 字段名”读取指定文本框并调用 `isFocused()`
- 当前 `ModularUi1TextFieldDetector` 与 `ModularUi2TextFieldDetector` 的默认白名单均设为空，等待后续按需补充
- 重写 `ModularUi1TextFieldDetectorTest` 与 `ModularUi2TextFieldDetectorTest`，覆盖白名单命中、未聚焦、非白名单忽略、默认空名单四种场景
- 重新通过 `./gradlew.bat test` 与 `./gradlew.bat assemble`，生成新的发布包

### 遇到的问题
- **原先 MUI 检测依赖对象图通用反射遍历**：虽然只在进游戏后启用，但仍不符合后续按名单渐进扩展的维护方式

### 已做决定
- MUI1/MUI2 的兼容策略保留，但入口改为显式白名单
- 后续新增兼容内容时，优先向各自 detector 的白名单添加 screen/field 项，而不是恢复通用扫描

---

## 2026-04-19：将原版输入框检测改为旧模组式白名单

### 已完成
- 将 `VanillaTextFieldDetector` 从通用反射扫描改为旧模组风格的保守白名单实现
- 原版常开输入法界面改为按固定界面类名匹配：`GuiChat`、`GuiEditSign`、`GuiCommandBlock`、`GuiCreateWorld`、`GuiScreenBook`、`GuiRenameWorld`、`GuiScreenAddServer`、`GuiScreenServerList`
- 原版需检查焦点的界面改为只读取固定字段：`GuiRepair.field_147091_w`、`GuiContainerCreative.searchField/field_147062_A`
- 新增 `VanillaTextFieldDetectorTest`，覆盖白名单常开、白名单字段聚焦、未聚焦、非白名单界面忽略四种场景
- 重新通过 `./gradlew.bat test` 与 `./gradlew.bat assemble`，生成新的发布包

### 遇到的问题
- **此前主菜单卡死在收窄 GUI 基类扫描后仍存在**：说明原版检测本身仍然过于激进，需要彻底放弃对任意原版 GUI 的通用反射遍历
- **测试代码误用了 `Set.of` / `Map.of`**：已改为 Java 8 兼容写法，确保和 1.7.10 目标工具链一致

### 已做决定
- 原版 GUI 不再追求“自动通用探测”，以稳定性优先，严格回到旧模组白名单策略
- MUI1/MUI2 保持通用检测，但仅在进入游戏后启用

---

## 2026-04-19：修复开始界面卡死问题

### 已完成
- 定位到开始界面卡死的根因是 `ReflectionWalker` 会沿 `GuiScreen` / `GuiContainer` 继承字段继续遍历，把扫描扩展到客户端重型对象图
- 为 `ReflectionWalker` 增加“遇到 GUI 基类即停止向上扫描继承字段”的保护，避免从界面对象进入 `Minecraft` 单例与渲染相关字段
- 新增 `ConditionalFocusDetector`，将 `ModularUi1TextFieldDetector` 与 `ModularUi2TextFieldDetector` 限制为仅在进入游戏后启用
- 补充 `ReflectionWalkerTest` 与 `ConditionalFocusDetectorTest` 回归测试
- 重新通过 `./gradlew.bat test` 与 `./gradlew.bat assemble`，生成修复后的 jar

### 遇到的问题
- **开始界面无异常堆栈但客户端卡死**：实质是主线程在每 tick 进行过深的反射对象图遍历，属于性能/遍历边界问题而非崩溃
- **直接在单测中实例化 `GuiScreen` 不稳定**：改为把“停止向上扫描的基类判定”抽成 `ReflectionWalker` 的可注入策略，再用假基类完成回归测试

### 已做决定
- `MUI1/MUI2` 检测默认仅在 `Minecraft.theWorld != null` 时启用
- `ReflectionWalker` 默认把 `GuiScreen` 与 `GuiContainer` 视为遍历边界，避免再次扫入 GUI 框架基础设施

---

## 2026-04-19：修复 ReflectionWalker 空指针并重新打包

### 已完成
- 修复 `ReflectionWalker` 在遍历对象图时把 `null` 子节点加入 `ArrayDeque` 导致的客户端 `NullPointerException`
- 为 `ReflectionWalker` 增加空值过滤逻辑，统一通过 `enqueueIfNotNull(...)` 处理字段、数组、`Iterable` 与 `Map` 子节点
- 新增 `ReflectionWalkerTest` 回归测试，覆盖“对象图中混有 null 子节点”场景
- 重新通过 `./gradlew.bat test` 验证修复未引入回归
- 重新执行 `./gradlew.bat assemble`，生成新的发布产物 `build/libs/inputmethodblockergtnh-0.1.0.jar`

### 遇到的问题
- **`ReflectionWalker` 默认假设所有反射子节点非空**：在 GTNH 客户端真实 GUI 对象图中该假设不成立，导致 tick 期间崩溃

### 已做决定
- `ReflectionWalker` 后续保持“跳过 null、继续遍历”的容错策略，而不是把空字段视为异常
- 重新打包时继续以 `assemble` 作为发布验证命令，确保 `jar` 与 `reobfJar` 都参与构建

---

## 2026-04-19：完成首版可编译实现

### 已完成
- 搭建 `InputMethodBlocker-GTNH` 的 GTNH 工程骨架、Gradle wrapper、资源目录与基础元数据
- 复制旧版 `InputMethodBlocker-Natives-x86.dll` 与 `InputMethodBlocker-Natives-x64.dll`
- 实现 `InputMethodBlockerGTNH`、`CommonProxy`、`ClientProxy` 与 `ClientEventHandler`
- 实现 `ImeBridge`、`PlatformInfo`、`NativeLibraryExtractor`、`LegacyNativeImeBindings`、`WindowsImeBridge`
- 新增兼容旧 DLL JNI 符号名的 `com.github.skystardust.InputMethodBlocker.NativeUtils`
- 实现 `InputFocusService`、`ReflectionWalker`、`VanillaTextFieldDetector`、`ModularUi1TextFieldDetector`、`ModularUi2TextFieldDetector`
- 完成元数据、平台判定、IME bridge、焦点聚合、MUI1/MUI2 探测、客户端事件处理的自动化测试
- 通过 `./gradlew.bat test` 与 `./gradlew.bat compileJava`

### 遇到的问题
- **GTNH 构建链要求根包目录存在且默认依赖 Git 版本推导**：通过创建根包目录并设置 `gtnh.modules.gitVersion = false` 解决
- **默认未携带 JUnit 5 依赖**：新增 `dependencies.gradle` 中的 `testImplementation` / `testRuntimeOnly`
- **`spotlessCheck` 异常失败**：当前在 `spotlessJava` 阶段抛出 Spotless/脚手架序列化异常，不是普通格式违规；已尝试 `--stacktrace`、`--no-configuration-cache` 与 `updateBuildScript`，仍未解决

### 已做决定
- 保留旧 JNI 类名以确保继续复用旧 DLL
- 客户端轮询使用 `FMLCommonHandler.instance().bus()` 上的 `ClientTickEvent`
- 当前把 `spotlessCheck` 视为脚手架级已知问题，后续单独排查

---

## 2026-04-19：迁移设计与项目初始化文档

### 已完成
- 梳理旧版 `InputMethodBlocker-master/1.7.x` 的功能与构建方式
- 明确新项目采用 GTNH 新工程重建，而不是在旧工程上直接升级
- 确认继续分发旧版 `InputMethodBlocker-Natives-x86.dll` 与 `InputMethodBlocker-Natives-x64.dll`
- 确认目标支持范围扩展为原版 GUI 与所有使用 MUI1 / MUI2 的模组文本输入框
- 在项目根目录建立 `docs/superpowers/specs/2026-04-19-inputmethodblocker-gtnh-design.md`
- 初始化 `log.md`、`ToDOLIST.md`、`context.md`

### 遇到的问题
- **`InputMethodBlocker-GTNH` 目前不是 git 仓库**：暂时无法按规范提交设计文档，需要后续在仓库初始化后再补版本管理

### 已做决定
- 采用“中度重构”方案
- 使用 GTNH 现代构建链支持 Java 17-25
- Java 侧重写为 `mod/proxy + ime + focus + compat` 分层结构
- MUI1 / MUI2 采用通用文本焦点探测，而不是维护模组白名单

---

## 2026-04-19：实现计划编写完成

### 已完成
- 编写 `docs/superpowers/plans/2026-04-19-inputmethodblocker-gtnh-implementation.md`
- 将实现拆分为工程骨架、IME bridge、原版探测、MUI1/MUI2 探测、客户端联调五个任务
- 为每个任务补充了测试、验证命令和建议提交点

### 遇到的问题
- **计划中的手动验证依赖 Windows 客户端环境**：自动化测试只能覆盖桥接状态机与反射探测逻辑，实际输入法切换仍需运行客户端验证

### 已做决定
- 将 `modId` 固定为 `inputmethodblockergtnh`
- 将根包名固定为 `com.github.skystardust.inputmethodblockergtnh`
- 优先采用反射式通用探测，减少对第三方 UI 内部细节的硬编码
## 2026-04-20：确认 native 重写设计并完成实施计划
### 已完成
- 编写并确认 `docs/superpowers/specs/2026-04-20-native-ime-rewrite-design.md`
- 编写 `docs/superpowers/plans/2026-04-20-native-ime-rewrite-implementation.md`
- 明确本次改动范围仅限 Windows x64 native DLL、Java x64 平台约束、资源打包与验证，不改动现有白名单 detector 架构
- 实现 `native/windows-x64/build-native.ps1` 与 `native/windows-x64/InputMethodBlocker-Natives-x64.cpp`
- 将 Java 侧 native 合约收敛为 Windows x64-only，并移除 `src/main/resources/InputMethodBlocker-Natives-x86.dll`
- 使用 MinGW-w64 成功重建 `src/main/resources/InputMethodBlocker-Natives-x64.dll`
- 重新通过 `./gradlew.bat test`、`./gradlew.bat compileJava`、`./gradlew.bat assemble`
- 重新生成发布产物 `build/libs/inputmethodblockergtnh-0.1.0.jar`

### 遇到的问题
- **当前工作目录不是 git 仓库**：无法按规范为本次 spec / plan 创建提交记录，只能先将文档落在项目目录中
- **本机 MSVC 环境缺少可用 Windows SDK 头文件**：`cl.exe` 能启动但编译 JNI 源码时缺少 `stdio.h`，最终改为使用已安装的 MinGW-w64 构建 DLL

### 已做决定
- native 层采用窗口级 IME context detach / restore 方案
- 保持 JNI ABI 不变，继续复用 `com.github.skystardust.InputMethodBlocker.NativeUtils`
- 后续实现只支持 Windows x64，并移除 x86 DLL 分发
- 为避免 `DllMain` + User32/IMM 带来的 loader lock 风险，不在 `DllMain` 中执行窗口恢复逻辑

---
