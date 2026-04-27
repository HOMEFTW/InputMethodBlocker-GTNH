# 项目上下文

## 基本信息
- 模组名称：InputMethodBlocker-GTNH
- 模组 ID：`inputmethodblockergtnh`
- 根包名：`com.github.skystardust.inputmethodblockergtnh`
- 目标环境：Minecraft 1.7.10 + GTNH，Java 17-25 运行环境
- 构建 JDK：通过 `JAVA_HOME` 提供，当前记录为 Zulu21
- 镜像配置：Gradle wrapper 使用 `https://mirrors.cloud.tencent.com/gradle/gradle-8.14.3-bin.zip`，Maven 解析入口使用 `https://mirrors.cloud.tencent.com/nexus/repository/maven-public/`
- 当前版本：`0.2.0`
- 当前阶段：核心实现、native x64 重写、首批白名单兼容、AE2/AE2Things/AE2FluidCraft Rework、NEI、Twist Space Technology 与 Programmable Hatches 白名单补全已完成；AE2Things 无线连接终端、无线二合一接口终端与背包终端剩余焦点已补齐；原版与 Angelica 通用搜索白名单的 `func_146206_l` 焦点方法遗漏已修复，仍待 Windows 客户端实机复测

## 已实现内容

### 机器
| 名称 | Meta ID | 类型 | 状态 |
|------|---------|------|------|
| 无 | - | - | 未实现 |

### 物品
| 名称 | 注册 | 描述 |
|------|------|------|
| 无 | - | 未实现 |

### 方块
| 名称 | 注册 | 描述 |
|------|------|------|
| 无 | - | 未实现 |

### 配方
| 配方池 | 类型 | 数量 |
|--------|------|------|
| 无 | - | 0 |

### 配置项
| 键 | 默认值 | 描述 |
|----|--------|------|
| 暂无 | - | 尚未定义 |

### Mixin
- 暂无

## 已实现代码模块
- 模组入口：`InputMethodBlockerGTNH`、`CommonProxy`、`ClientProxy`
- 客户端事件：`client/ClientEventHandler`
- IME 层：`ImeBridge`、`DisabledImeBridge`、`PlatformInfo`、`NativeLibraryExtractor`、`NativeLibraryLoader`、`NativeImeBindings`、`LegacyNativeImeBindings`、`WindowsImeBridge`
- 兼容旧 DLL JNI：`com.github.skystardust.InputMethodBlocker.NativeUtils`
- 焦点层：`FocusDetector`、`InputFocusService`
- 条件检测层：`ConditionalFocusDetector`
- 兼容层：`VanillaTextFieldDetector`、`ModSearchTextFieldDetector`、`AeTerminalTextFieldDetector`、`ModularUi1TextFieldDetector`、`ModularUi2TextFieldDetector`
- 兼容辅助层：`WhitelistedReflectiveTextFieldDetector`
- native 源码与脚本：`native/windows-x64/InputMethodBlocker-Natives-x64.cpp`、`native/windows-x64/build-native.ps1`

## 白名单兼容状态
- `VanillaTextFieldDetector` 使用旧模组式原版 GUI 白名单，不再对任意原版 GUI 做通用反射扫描；聚焦文本框的焦点方法支持 `isFocused` 与 Minecraft 1.7.10 运行时 obf 名 `func_146206_l`。
- `ModSearchTextFieldDetector` 已实现 Angelica、NEI、ServerUtilities 的首批搜索框与输入框白名单，支持实例字段、静态字段与静态入口后的点分字段路径；焦点方法支持 `isFocused`、Minecraft 1.7.10 运行时 obf 名 `func_146206_l` 与 NEI 旧式 `focused`。
- `AeTerminalTextFieldDetector` 已实现 AE2 / AE2Things 终端搜索框白名单，支持 `net.minecraft.client.gui.GuiTextField`、`appeng.client.gui.widgets.MEGuiTextField`、`com.asdflj.ae2thing.client.gui.widget.METextField`、`com.asdflj.ae2thing.client.gui.widget.THGuiTextField`、`com.glodblock.github.client.gui.FCGuiTextField`；焦点方法支持 `isFocused` 与 Minecraft 1.7.10 运行时 obf 名 `func_146206_l`。
- `AeTerminalTextFieldDetector` 当前覆盖 AE2 原生 `GuiAmount.amountTextField`、`GuiCellRestriction.amountField/typesField`、`GuiCraftingCPU.searchField`、`GuiCraftConfirm.searchField`、`GuiLevelEmitter.amountTextField`、`GuiMEMonitorable.searchField`、`GuiOptimizePatterns.amountToCraft`、`GuiOreFilter.textField`、`GuiPatternItemRenamer.textField`、`GuiQuartzKnife.textField`、`GuiRenamer.textField`、`GuiInterfaceTerminal` 三个搜索字段。
- `AeTerminalTextFieldDetector` 当前覆盖 AE2Things `GuiMonitor.searchField`、`GuiBaseInterfaceWireless` 三个搜索字段、`GuiWirelessConnectorTerminal.searchField/components.textField/clickables`、`GuiWirelessDistributor.searchField/components.textField`、`GuiWirelessDualInterfaceTerminal.searchFieldInputs/searchFieldOutputs/searchFieldNames/itemPanel.searchField/panels.searchField`、背包终端 `GuiCraftingTerminal.searchField`、`GuiAmount.amountBox`、`GuiPatternValueName.textField`、`GuiRenamer.textField`、`GuiFluidPacketEncoder.level`。
- `AeTerminalTextFieldDetector` 当前覆盖 Fluid Craft / AE2FluidCraft Rework 兼容路径：`com.glodblock.github.client.gui.base.FCGuiAmount.amountBox`、`com.glodblock.github.client.gui.base.FCGuiMonitor.searchField`、`com.glodblock.github.client.gui.GuiLevelTerminal.searchFieldOutputs/searchFieldNames`、`com.glodblock.github.client.gui.GuiLevelMaintainer.focusedWidget.textField`、`com.glodblock.github.client.gui.GuiFluidLevelEmitter.amountTextField`、`com.glodblock.github.client.gui.GuiRenamer.textField`、`com.glodblock.github.client.gui.GuiMagnetFilter.oreDict`。
- `ModSearchTextFieldDetector` 当前覆盖 NEI `LayoutManager.searchField/quantity`、`GuiRecipe.searchField`、`GuiPotionCreator.durationField`、`GuiOptionList.slot.options.textField`、`GuiPresetSettings.leftPanel.nameField/rightPanel.searchField`、`DebugHandlerWidget.instance.container.widgets`。
- `ModularUi1TextFieldDetector` 已覆盖 MUI1 运行时焦点路径 `com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui.context.cursor.focused`，用于识别当前聚焦的 `com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget` 与 `com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget` 后代；该路径覆盖 Twist Space Technology 中 `GT_Hatch_WirelessData_input`、`DynamicSpeedController`、`DynamicParallelController`、`TST_MegaCraftingCenter`、`TST_StrangeMatterAggregator` 等 MUI1 文本输入场景，并覆盖 Programmable Hatches 中原生 MUI1 `NumericWidget` 与 `reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget` 数值输入场景。
- `ModularUi2TextFieldDetector` 已改为白名单 screen/field 检测，当前默认白名单为空。
- `WhitelistedReflectiveTextFieldDetector` 已支持继承链类型匹配、静态字段白名单、多焦点方法名、多字段任一命中、点分字段路径、数组与 `Iterable` 候选展开。

## 架构说明
- 已按 `mod/proxy`、`ime`、`focus`、`compat` 四层拆分。
- `ime` 层统一负责 Windows DLL 加载和 native 调用。
- `WindowsImeBridge` 已对齐旧模组初始化语义：成功加载 DLL 后立即执行一次 `inactiveInputMethod("")`，并同步 `currentActive = false`。
- `InputFocusService` 已改为对 `inactive` 状态持续发布，配合 `WindowsImeBridge` 的重复 `inactive` 调用，在非输入状态下持续压制 IME。
- `ClientEventHandler` 与 `WindowsImeBridge` 已输出诊断日志，用于区分焦点层、bridge 层与 native 层问题。
- 除 `VanillaTextFieldDetector` 外，其余 detector 均作为 compat 层，由 `ClientProxy` 按模组是否已加载或类是否可用决定是否注册。
- 运行产物中已移除遗留的通用反射扫描实现，所有 GUI 输入焦点检测都必须通过白名单扩展。

## 已验证命令
- `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.VanillaTextFieldDetectorTest --tests com.github.skystardust.inputmethodblockergtnh.compat.ModSearchTextFieldDetectorTest`
- `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.*`
- `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.AeTerminalTextFieldDetectorTest`
- `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.AeTerminalTextFieldDetectorTest --tests com.github.skystardust.inputmethodblockergtnh.compat.ModSearchTextFieldDetectorTest`
- `./gradlew.bat test --tests com.github.skystardust.inputmethodblockergtnh.compat.ModularUi1TextFieldDetectorTest`
- `./gradlew.bat test assemble`
- `./gradlew.bat assemble`

## 当前打包产物
- 运行用 jar：`build/libs/inputmethodblockergtnh-0.2.0.jar`
- 开发环境 jar：`build/libs/inputmethodblockergtnh-0.2.0-dev.jar`
- 源码 jar：`build/libs/inputmethodblockergtnh-0.2.0-sources.jar`

## 当前未完成项
- Windows 客户端实机验证主菜单、游戏内非输入状态、原版白名单、AE2、AE2Things、Angelica、NEI、ServerUtilities、MUI1、MUI2 的 IME 行为。
- 为 MUI1 / MUI2 补充首批实际 screen/field 白名单。
- 排查并修复 `spotlessCheck` 的 Spotless/脚手架异常。
