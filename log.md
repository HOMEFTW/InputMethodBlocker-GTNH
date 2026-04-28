# 开发日志

## 2026-04-28：发布 `0.2.1`

### 已完成
- 将 `gradle.properties` 中的 `modVersion` 提升到 `0.2.1`。
- 准备重新执行 `test assemble`，生成 `inputmethodblockergtnh-0.2.1.jar` 作为 GitHub Release 资产。
- 准备将 AE 系选择数量界面焦点白名单排除修复推送到 GitHub，并创建 `0.2.1` 发布版本。

### 已做决定
- 本次 release 使用标签 `v0.2.1`，Release 标题使用 `0.2.1`，运行用资产为非 `dev`、非 `sources` 的 `inputmethodblockergtnh-0.2.1.jar`。

---

## 2026-04-28：移除 AE 系选择数量界面焦点白名单

### 已完成
- 重新对照 AE2、AE2Things 与 AE2FC 源码核对截图 `2026-04-28_09.11.32.png` 对应的数量选择界面。
- 确认非 AE2Things 普通无线终端下单会走 AE2 原生 `appeng.client.gui.implementations.GuiCraftAmount`，焦点字段为其基类 `appeng.client.gui.implementations.GuiAmount.amountTextField`。
- 确认 AE2Things 的 `MixinGuiCraftAmount` 只把 AE2 原生 `GuiCraftAmount` 混入为 `IGuiCraftAmount`，不会改变运行时 screen class。
- 按用户要求将 AE2、AE2Things、AE2FC 三个模组的“选择数量 / 下单数量”界面全部移出焦点白名单。
- 精确排除 `appeng.client.gui.implementations.GuiCraftAmount`、`com.asdflj.ae2thing.client.gui.GuiCraftAmount` 与 `com.glodblock.github.client.gui.GuiFluidCraftAmount`。
- 回归测试覆盖 `defaultWhitelistIgnoresAe2CraftAmountField`、`defaultWhitelistIgnoresAe2ThingsCraftAmountField` 与 `defaultWhitelistIgnoresAe2FluidCraftAmountField`。

### 遇到的问题
- **AE2Things mixin 容易误导判断**：AE2Things 会 mixin AE2 原生 `GuiCraftAmount`，但普通 AE2 终端下单界面的实机 screen class 仍是 `appeng.client.gui.implementations.GuiCraftAmount`；AE2Things 自己的无线二合一/灌注样板终端则使用 `com.asdflj.ae2thing.client.gui.GuiCraftAmount`；AE2FC 使用 `com.glodblock.github.client.gui.GuiFluidCraftAmount`。
- **白名单入口在基类上**：直接移除 `appeng.client.gui.implementations.GuiAmount.amountTextField` 会影响其它继承 `GuiAmount` 的 AE2 数量/配置界面，因此本次使用精确排除，而不是删除整个基类白名单。

### 已做决定
- 对选择数量界面采用精确 screen 排除，不移除 `GuiAmount` / `FCGuiAmount` 基类白名单，避免误伤其它数量/配置界面。

---

## 2026-04-27：发布 `0.2.0`

### 已完成
- 将 `gradle.properties` 中的 `modVersion` 提升到 `0.2.0`。
- 准备重新执行 `test assemble`，生成 `inputmethodblockergtnh-0.2.0.jar` 作为 GitHub Release 资产。
- 准备将当前白名单修复推送到 GitHub，并创建 `0.2.0` 发布版本。

### 已做决定
- 本次 release 使用标签 `v0.2.0`，Release 标题使用 `0.2.0`，运行用资产为非 `dev`、非 `sources` 的 `inputmethodblockergtnh-0.2.0.jar`。

---

## 2026-04-27：检查并修复其他白名单焦点方法遗漏

### 已完成
- 审计当前所有白名单 detector，重点检查是否存在支持 `net.minecraft.client.gui.GuiTextField` 却只调用 `isFocused` 的同类问题。
- 确认 `VanillaTextFieldDetector` 与 `ModSearchTextFieldDetector` 仍存在同类风险：原版修复铁砧/创造搜索框、Angelica `FontConfigScreen.searchBox/testArea` 等入口在 1.7.10 重混淆运行时可能只暴露 `func_146206_l`。
- 为 `VanillaTextFieldDetector` 增加 `isFocused` / `func_146206_l` 双焦点方法回退。
- 为 `ModSearchTextFieldDetector` 的默认焦点方法集增加 `func_146206_l`，保留 `isFocused` 与 NEI 旧式 `focused` 支持。
- 增加原版混淆焦点方法测试与 Angelica `GuiTextField` 默认白名单回归测试；先确认红灯，再修复到绿灯。

### 遇到的问题
- **通用搜索白名单包含原版 `GuiTextField` 类型**：Angelica `FontConfigScreen` 直接使用 Minecraft `GuiTextField`，因此与 AE2Things `THGuiTextField` 一样会受重混淆焦点方法名影响。
- **ModularUI 白名单不属于同类问题**：MUI1/MUI2 当前检测的是自身 widget 的 `isFocused`，不是继承自 Minecraft `GuiTextField` 的焦点方法。

### 已做决定
- 只为实际包含原版 `GuiTextField` 的原版/通用搜索/AE 白名单补充 `func_146206_l`；MUI1/MUI2 保持现有 `isFocused` 路径，不额外扩大匹配面。

---

## 2026-04-27：修复 AE2Things `THGuiTextField` 实机焦点识别

### 已完成
- 根据实机反馈重新定位 AE2Things 无线连接终端、无线二合一接口终端与背包终端搜索框未命中的根因。
- 确认这些失败入口共同使用 `com.asdflj.ae2thing.client.gui.widget.THGuiTextField`，该类继承自 Minecraft/NEI 文本框，运行时焦点方法会使用 1.7.10 obf 名 `func_146206_l`。
- 为 `AeTerminalTextFieldDetector` 增加 `func_146206_l` 焦点方法支持，保留原有 `isFocused` 支持。
- 将测试桩改为模拟实机 `THGuiTextField` 焦点方法名，并覆盖无线连接终端搜索框、无线二合一接口名搜索框、无线二合一物品搜索框、背包终端搜索框。

### 遇到的问题
- **上一轮测试桩过于理想化**：测试桩直接提供 `isFocused()`，但实机中 `THGuiTextField` 的焦点方法来自 Minecraft `GuiTextField`，重混淆后不是这个方法名，导致单元测试通过而实机未命中。

### 已做决定
- 仅在 AE 兼容 detector 中增加 `func_146206_l` 作为焦点方法别名，不扩大白名单字段范围，也不恢复通用对象图扫描。

---

## 2026-04-27：补全 AE2Things 三个终端剩余焦点白名单

### 已完成
- 对照 `D:\Code\GTNH LIB\AE2Things-main` 复查无线连接终端、无线二合一接口终端与背包终端源码。
- 为 `GuiWirelessConnectorTerminal` 补充 `clickables` 列表入口，覆盖组件名称编辑框以 `METextField` 直接挂在点击组件列表中的情况。
- 为 `GuiWirelessDualInterfaceTerminal` 显式补充 `searchFieldInputs/searchFieldOutputs/searchFieldNames` 与 `panels.searchField`，覆盖继承搜索框和侧边物品面板搜索框。
- 为背包终端实际使用的 `GuiCraftingTerminal` 显式补充 `searchField`。
- 添加无线连接终端 `clickables` 名称输入框与背包终端搜索框的回归测试。

### 遇到的问题
- **无线连接终端的名称输入框有两条可达路径**：源码中 `Component.textField` 同时也加入 `clickables`，原白名单只覆盖 `components.textField`，在实际焦点排查时不够完整。
- **背包终端实际复用 `GuiCraftingTerminal`**：虽然 `GuiMonitor.searchField` 可通过继承链覆盖，但白名单缺少背包终端类本身的显式记录，排查时容易遗漏。

### 已做决定
- 保持显式字段路径策略，只为 AE2Things 已确认的终端类补充入口，不扩大为任意 AE2Things GUI 扫描。

---

## 2026-04-27：重新打包当前兼容白名单版本

### 已完成
- 执行 `./gradlew.bat assemble` 重新生成当前版本 jar。
- 确认运行用 jar 为 `build/libs/inputmethodblockergtnh-0.1.0.jar`。
- 同步生成 `inputmethodblockergtnh-0.1.0-dev.jar` 与 `inputmethodblockergtnh-0.1.0-sources.jar`。

### 遇到的问题
- **沙箱环境无法写入用户级 Gradle wrapper 缓存锁文件**：首次普通执行失败，改用已授权的 Gradle 执行权限完成打包。

### 已做决定
- 继续使用 `assemble` 作为发布前打包入口，实际放入客户端 `mods` 目录的文件为非 `dev`、非 `sources` 的 `inputmethodblockergtnh-0.1.0.jar`。

---

## 2026-04-27：补全 Programmable Hatches MUI1 数值输入焦点白名单

### 已完成
- 对照 `D:\Code\GTNH LIB\Programmable-Hatches-Mod-290-daily-latest` 扫描 MUI1 `TextFieldWidget` 与 `NumericWidget` 使用点。
- 确认 Programmable Hatches 的自定义 `reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget` 继承自 MUI1 `BaseTextFieldWidget`，原白名单只匹配 `TextFieldWidget` 会漏掉该类数值输入焦点。
- 将 `ModularUi1TextFieldDetector` 的受支持输入框类型扩展为 `TextFieldWidget` 与 `BaseTextFieldWidget`，通过继承链匹配覆盖原生 MUI1 `NumericWidget` 与 Programmable Hatches 自定义 `NumericWidget`。
- 添加 Programmable Hatches 自定义 `NumericWidget` 位于 `ModularGui.context.cursor.focused` 时的回归测试。

### 遇到的问题
- **Programmable Hatches 的部分数值输入不是 `TextFieldWidget` 子类**：这些焦点对象挂在 MUI1 运行时 cursor 上，但实际类型继承自 `BaseTextFieldWidget`，需要按 MUI1 文本输入基类覆盖。

### 已做决定
- 继续沿用 `ModularGui.context.cursor.focused` 显式白名单路径，只扩大可识别的 MUI1 文本输入基类，不恢复窗口 children 的通用扫描。

---

## 2026-04-27：审计并补全 AE2 与 NEI 输入焦点白名单

### 已完成
- 对照 `D:\Code\GTNH LIB\Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH` 扫描 `MEGuiTextField` / `GuiTextField` 使用点，补全 AE2 原生 GUI 漏项。
- 新增覆盖 AE2 `GuiCellRestriction.amountField/typesField`、`GuiOreFilter.textField`、`GuiLevelEmitter.amountTextField`、`GuiOptimizePatterns.amountToCraft`、`GuiRenamer.textField`、`GuiQuartzKnife.textField`、`GuiPatternItemRenamer.textField`。
- 对照 `D:\Code\GTNH LIB\NotEnoughItems-master` 扫描 NEI `TextField` / `SearchField` 使用点，补全配置页、Preset 页和 debug handler 面板漏项。
- 新增覆盖 NEI `GuiOptionList.slot.options.textField`、`GuiPresetSettings.leftPanel.nameField/rightPanel.searchField`、`DebugHandlerWidget.instance.container.widgets`。
- 为静态白名单增加点分字段路径支持，使静态入口后面的嵌套 widget 列表也可被显式白名单检测。

### 遇到的问题
- **AE2 原生 GUI 不止终端搜索框与通用数量框**：部分独立配置/命名 GUI 使用 `textField` 或独立的 `GuiTextField` 字段，原白名单未包含。
- **NEI 的部分输入框挂在子组件或静态 debug widget 后面**：配置项文本框位于 `GuiOptionList` 的 option 列表内，debug handler 输入框位于静态 `DebugHandlerWidget.instance` 的嵌套容器内。

### 已做决定
- 继续使用显式字段路径白名单；仅扩展静态字段白名单的点分路径能力，不恢复任意对象图扫描。

---

## 2026-04-27：补全 Twist Space Technology ModularUI 输入焦点白名单

### 已完成
- 对照 `D:\Code\GTNH LIB\Twist-Space-Technology-Mod-main` 中的 `TextFieldWidget` 使用点，确认 TST 输入焦点来自 ModularUI1 运行时窗口。
- 为 `ModularUi1TextFieldDetector` 新增默认白名单路径：`com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui.context.cursor.focused`。
- 覆盖 TST 中 `GT_Hatch_WirelessData_input`、`DynamicSpeedController`、`DynamicParallelController`、`TST_MegaCraftingCenter`、`TST_StrangeMatterAggregator` 等 MUI1 文本输入场景。
- 添加 MUI1 运行时焦点路径回归测试，确认聚焦的 `TextFieldWidget` 会命中，未聚焦输入框不会命中。

### 遇到的问题
- **TST 的输入框不是机器类成员字段**：`TextFieldWidget` 直接加入 `ModularWindow.Builder`，运行时焦点保存在 `ModularGui.context.cursor.focused`，不能按 TST 机器类字段白名单处理。

### 已做决定
- MUI1 白名单只读取当前 cursor 的 focused widget，不扫描窗口 children 列表，避免恢复宽泛对象图扫描。

---

## 2026-04-27：补全 AE2FluidCraft Rework 输入框白名单

### 已完成
- 对照 `D:\Code\GTNH LIB\AE2FluidCraft-Rework-1.4.120-gtnh` 中的 GUI 输入框声明，补全 `AeTerminalTextFieldDetector` 的 Fluid Craft 白名单覆盖。
- 新增覆盖 `FCGuiMonitor.searchField`、`GuiLevelTerminal.searchFieldOutputs/searchFieldNames`、`GuiLevelMaintainer.focusedWidget.textField`、`GuiFluidLevelEmitter.amountTextField`、`GuiRenamer.textField`、`GuiMagnetFilter.oreDict`。
- 为上述 AE2FluidCraft Rework 焦点路径添加回归测试桩，并确认默认白名单可以命中聚焦输入框。

### 遇到的问题
- **Fluid Craft 输入框分散在基类、继承类与嵌套 widget 中**：仅覆盖 `FCGuiAmount.amountBox` 会漏掉终端搜索、液体等级发信器、重命名器、磁铁过滤器与 Level Maintainer 的实际焦点路径。

### 已做决定
- 继续使用显式 screen/field 白名单；对嵌套焦点使用点分字段路径 `focusedWidget.textField`，不恢复通用对象图扫描。

---

## 2026-04-27：修复三份项目文档编码

### 已完成
- 将 `log.md`、`ToDOLIST.md`、`context.md` 从乱码工作区状态恢复为中文内容。
- 使用 `HEAD` 中仍保持正常中文的版本作为基底，并补回 2026-04-22 构建配置记录与 2026-04-27 AE2Things 白名单补全记录。
- 使用 UTF-8 无 BOM 写回三份文档，避免 PowerShell 默认编码再次污染中文内容。

### 遇到的问题
- **工作区版本已经被错误编码写坏**：文件中出现典型 mojibake 标记，无法作为可靠来源继续编辑。

### 已做决定
- 后续维护三份项目文档时必须显式使用 UTF-8 无 BOM 写入，避免 `Set-Content`、`Out-File` 或默认重定向造成编码漂移。

---

## 2026-04-27：补全 AE2Things 相关输入框白名单

### 已完成
- 对照 `D:\Code\GTNH LIB\AE2Things-main` 中的 GUI 与 mixin 引用，补全 `AeTerminalTextFieldDetector` 白名单。
- 新增支持 `appeng.client.gui.implementations.GuiAmount.amountTextField`、`GuiCraftingCPU.searchField`、`GuiCraftConfirm.searchField`。
- 新增支持 Fluid Craft 兼容路径：`com.glodblock.github.client.gui.FCGuiTextField` 与 `com.glodblock.github.client.gui.base.FCGuiAmount.amountBox`。
- 为 AE2 原生数量输入、AE2Things 继承的合成状态/确认搜索框、Fluid Craft 数量输入添加回归测试桩。
- 重新通过 `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.AeTerminalTextFieldDetectorTest` 与 `./gradlew.bat test assemble`。

### 遇到的问题
- **AE2Things 的输入框不全在自身 GUI 类中声明**：部分输入框来自 AE2 原生 GUI 继承路径或 Fluid Craft mixin 兼容路径，原先仅覆盖终端搜索框会漏掉这些界面。

### 已做决定
- 继续沿用显式白名单策略，不恢复通用反射扫描。
- 对 AE2Things 项目中通过继承或 mixin 触达的 AE2 / Fluid Craft 输入框，也归入 `AeTerminalTextFieldDetector` 的 AE 兼容范围。

---

## 2026-04-22：构建 JDK 切换为 JAVA_HOME / Zulu21

### 已完成
- 在 `gradle.properties` 中加入 `org.gradle.java.installations.fromEnv = JAVA_HOME`，让 Gradle Java 探测显式跟随环境变量。
- 当前构建入口要求 `JAVA_HOME` 指向 Zulu21。
- 保持 Gradle 与 Maven 镜像配置不变。

### 已做决定
- 不在仓库中硬编码本机 JDK 路径，后续只通过 `JAVA_HOME` 管理构建 JDK。

---

## 2026-04-22：切换 Gradle 与 Maven 到腾讯云镜像

### 已完成
- 将 `gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` 切换为 `https://mirrors.cloud.tencent.com/gradle/gradle-8.14.3-bin.zip`。
- 在 `settings.gradle` 的 `pluginManagement.repositories` 中加入 `Tencent Maven Mirror`，并用 `https://mirrors.cloud.tencent.com/nexus/repository/maven-public/` 替换默认 `mavenCentral()` 入口。

### 遇到的问题
- 本次只调整项目级 Gradle wrapper 与 plugin/dependency 解析入口，没有创建用户级 `~/.gradle/init.gradle`，避免影响其他 Gradle 项目。

### 已做决定
- Gradle 分发包使用腾讯云 `/gradle` 镜像，Maven 依赖解析使用腾讯云 `/nexus/repository/maven-public/` 镜像。

---
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
