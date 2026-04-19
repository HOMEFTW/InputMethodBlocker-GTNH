# 项目上下文

## 基本信息
- 模组名称：InputMethodBlocker-GTNH
- 模组 ID：`inputmethodblockergtnh`
- 根包名：`com.github.skystardust.inputmethodblockergtnh`
- 目标环境：Minecraft 1.7.10 + GTNH，Java 17-25 运行环境
- 当前阶段：首版实现已完成并完成修复后重新打包，待 Windows 客户端手动验证

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
- 测试：元数据、平台判定、IME bridge、焦点聚合、`FocusArchitectureGuardTest`、`ConditionalFocusDetector`、`VanillaTextFieldDetector` 白名单回归、`ModSearchTextFieldDetector` 白名单回归、`AeTerminalTextFieldDetector` 白名单回归、MUI1/MUI2 白名单回归、客户端事件处理

## 依赖与兼容目标
- 计划使用 GTNH 标准构建链 `gtnhconvention`
- 计划兼容原版 `GuiTextField`
- 计划兼容 MUI1：`com.gtnewhorizons.modularui`
- 计划兼容 MUI2：`com.cleanroommc.modularui`
- 继续分发旧版 Windows DLL：`InputMethodBlocker-Natives-x86.dll`、`InputMethodBlocker-Natives-x64.dll`
- 测试依赖：`org.junit.jupiter:junit-jupiter-api:5.10.2`、`org.junit.jupiter:junit-jupiter-engine:5.10.2`

## 架构说明
- 已按 `mod/proxy`、`ime`、`focus`、`compat` 四层拆分
- `ime` 层统一负责 Windows DLL 加载和 native 调用
- `WindowsImeBridge` 已对齐旧模组初始化语义：成功加载 DLL 后立即执行一次 `inactiveInputMethod("")`，并同步 `currentActive = false`
- `focus` 层统一输出“当前是否应启用输入法”
- `InputFocusService` 现已改为对 `inactive` 状态持续发布，配合 `WindowsImeBridge` 的重复 `inactive` 调用，在非输入状态下持续压制 IME
- `ClientEventHandler` 现已输出诊断日志：screen 类名、命中的 detector、期望 IME 状态与 bridge 可用性
- `WindowsImeBridge` 现已输出诊断日志：初始化成功、native 调用计数、bridge 不可用告警
- `ConditionalFocusDetector` 用于按运行条件启用特定 detector，当前用于限制 AE / MUI 检测仅在游戏内生效
- `VanillaTextFieldDetector` 已改为旧模组式白名单原版 GUI 检测，不再对任意原版 GUI 做通用反射扫描
- 除 `VanillaTextFieldDetector` 外，其余 detector 均作为 compat 层，由 `ClientProxy` 按模组是否已加载或类是否可用再决定是否注册
- `ModSearchTextFieldDetector` 已实现 Angelica、NEI、ServerUtilities 的首批搜索框与输入框白名单，支持实例字段与静态字段两种白名单形式，并仅在相关模组已加载时注册
- `AeTerminalTextFieldDetector` 已实现 AE2 / AE2Things 终端搜索框白名单，支持 `MEGuiTextField` 与 `THGuiTextField`，并仅在 AE2 / AE2Things 已加载时注册
- `ModularUi1TextFieldDetector` 与 `ModularUi2TextFieldDetector` 已改为白名单 screen/field 检测，当前默认白名单为空，并仅在对应类可用时注册
- `WhitelistedReflectiveTextFieldDetector` 已支持继承链类型匹配、静态字段白名单、多焦点方法名与多字段任一命中
- 运行产物中已移除遗留的通用反射扫描实现，所有 GUI 输入焦点检测都必须通过白名单扩展
- `compat` 层负责普通模组 GUI、AE 终端、MUI1/MUI2 的白名单文本焦点探测；原版 GUI 独立归为基础层
- 已确认实现计划文档：`docs/superpowers/plans/2026-04-19-inputmethodblocker-gtnh-implementation.md`
- `ClientProxy` 通过 `FMLCommonHandler.instance().bus()` 注册 `ClientEventHandler`
- 当前已验证 `test` 与 `compileJava` 通过
- 当前已验证 `assemble` 通过，最新产物位于 `build/libs/inputmethodblockergtnh-0.1.0.jar`
- 当前未完成项：Windows 客户端验证开始界面不卡死与输入框切换、`spotlessCheck` 异常排查
## 当前实现状态补充
- 已新增 native 重写设计文档：`docs/superpowers/specs/2026-04-20-native-ime-rewrite-design.md`
- 已新增 native 重写实施计划：`docs/superpowers/plans/2026-04-20-native-ime-rewrite-implementation.md`
- 已实现 native 构建脚本：`native/windows-x64/build-native.ps1`
- 已实现 native 源码：`native/windows-x64/InputMethodBlocker-Natives-x64.cpp`
- 已移除 `src/main/resources/InputMethodBlocker-Natives-x86.dll`
- 已重建 `src/main/resources/InputMethodBlocker-Natives-x64.dll`

## 架构补充说明
- 下一阶段将只替换 Windows x64 native DLL，保持 Java 侧白名单 detector 与 compat 层架构不变
- 新 native 设计采用窗口级 IME context detach / restore，目标是在非白名单状态下阻断 `Shift` 等切换键使 IME 挂回 Minecraft 窗口
- 继续复用 `com.github.skystardust.InputMethodBlocker.NativeUtils` 的 JNI ABI，避免大规模改动 Java 桥接层
- 当前 native 构建脚本优先使用已安装的 MinGW-w64，找不到时再尝试 MSVC Build Tools
- 当前产物已重新打包为 `build/libs/inputmethodblockergtnh-0.1.0.jar`

---
