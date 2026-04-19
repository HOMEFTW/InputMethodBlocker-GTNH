# InputMethodBlocker-GTNH Design

**日期：** 2026-04-19

## 目标

将老旧的 `InputMethodBlocker-master` 迁移为新的 GTNH 1.7.10 客户端模组工程 `InputMethodBlocker-GTNH`，使用 GTNH 现代构建链支持 Java 17-25 开发与运行环境，并保持 Windows 平台下原有“在文本输入场景自动切换输入法状态”的功能可用。

## 范围

本次设计覆盖以下内容：

- 新项目的 GTNH 工程结构与基础构建方式
- 旧版 1.7.x 模组逻辑到新项目的迁移策略
- 旧 DLL 的继续分发与 Java 侧桥接封装
- 原版 GUI、MUI1、MUI2 的通用文本输入焦点探测
- 非 Windows 环境的安全降级策略
- 验证目标与后续实现边界

本次设计不包含以下内容：

- 重写旧 DLL 的原生实现
- 为完全自定义的第三方 GUI 单独编写兼容名单
- 增加与输入法控制无关的新功能

## 背景与现状

旧版 `InputMethodBlocker-master` 的 `1.7.x` 版本是一个纯客户端 Forge 模组，核心逻辑很简单：

- 在 `preInit` 中注册 GUI 事件监听并解包 DLL
- 在 `init` 中调用 native 方法关闭输入法
- 在聊天、告示牌、书本、世界重命名、服务器地址输入、铁砧重命名、创造搜索框等场景中，根据当前是否处于文本输入状态调用 native 方法开关输入法

旧项目的问题主要有：

- 使用旧的 `ForgeGradle 1.2` 工程结构，不适合直接提升到 GTNH 现代 Java 17+ 工具链
- 主类、事件监听和 native 加载逻辑耦合在一起
- `OSChecker` 与 native 加载逻辑较粗糙，异常保护不足
- 只覆盖原版 GUI 和少量硬编码界面，无法满足 GTNH 中大量 `ModularUI` / `ModularUI2` 输入框场景

## 决策摘要

经过方案讨论，本项目采用以下已确认决策：

- 采用“中度重构”路线，而不是直接复制旧工程
- 新建 `InputMethodBlocker-GTNH` 作为独立 GTNH 工程
- 继续分发旧版 `x86/x64 DLL`，但不假设拥有其源码
- 支持目标扩展到“所有使用 MUI1 / MUI2 的模组输入框”
- 支持方式以“通用自动支持”为目标，而不是维护模组白名单

## 方案比较

### 方案 A：GTNH 新工程重建 + Java 侧重写 + 复用旧 DLL

这是推荐方案。

优点：

- 完整对齐 GTNH 现代 1.7.10 Java 17+ 工具链
- 结构清晰，后续维护成本最低
- 可以把 GUI 探测逻辑从“硬编码界面列表”升级为“通用文本焦点探测”

缺点：

- 需要把旧项目的 Java 逻辑重新整理，而不是直接复制

### 方案 B：复制旧 1.7.x 工程后逐步改造成 GTNH 工程

优点：

- 看起来迁移路径比较直观

缺点：

- 很容易把旧工程包袱一起带入新项目
- 最终仍然大概率需要二次清理结构

### 方案 C：仅包一层 GTNH 外壳，尽量原样嵌入旧逻辑

优点：

- 初始改动最少

缺点：

- 技术债最高
- 很难优雅扩展到 MUI1 / MUI2
- Java 17+ 兼容风险最大

最终采用方案 A。

## 总体架构

新项目将以 GTNH 标准模组结构重建，并把逻辑拆成 4 个清晰模块：

### 1. Mod / Proxy 层

职责：

- 模组主类与生命周期入口
- 客户端/服务端代理拆分
- 配置入口与日志入口
- 客户端侧事件注册

设计要点：

- 主类只负责生命周期分发
- 所有客户端行为只在 `ClientProxy` 中注册
- 服务端环境不加载客户端 GUI 探测逻辑

### 2. IME Bridge 层

职责：

- 管理 Windows native DLL 的解包、加载和调用
- 对 Java 层提供统一接口，如 `setImeActive(boolean active)`
- 在 DLL 不可用、平台不支持、调用失败时安全降级

设计要点：

- 统一收口到 `WindowsImeBridge`
- 只允许加载一次，并缓存可用状态
- 所有 native 调用均带异常保护
- 日志中明确记录“当前平台”“选择的 DLL”“是否加载成功”

### 3. Focus Detection 层

职责：

- 统一判定当前客户端是否存在“应启用输入法的文本输入焦点”
- 根据状态变化触发 IME bridge

设计要点：

- 不再为每个 GUI 单独硬编码 if/instanceof
- 统一输出布尔结果：当前是否应该启用输入法
- 只有在状态发生变化时才调用 native，避免无意义重复切换

### 4. Compatibility 层

职责：

- 分别适配原版 GUI、MUI1、MUI2 的文本输入控件
- 为 Focus Detection 层提供统一探测接口

设计要点：

- 原版、MUI1、MUI2 各自独立实现
- 不依赖模组白名单
- 某一兼容层失败时不影响其他兼容层继续工作

## 项目结构

建议的新项目结构如下：

```text
InputMethodBlocker-GTNH/
  build.gradle
  gradle.properties
  settings.gradle
  src/main/java/<package>/
    InputMethodBlockerGTNH.java
    CommonProxy.java
    ClientProxy.java
    compat/
      CompatibilityDetector.java
      VanillaTextFieldDetector.java
      ModularUi1TextFieldDetector.java
      ModularUi2TextFieldDetector.java
    focus/
      InputFocusService.java
      ScreenFocusSnapshot.java
    ime/
      ImeBridge.java
      WindowsImeBridge.java
      NativeLibraryExtractor.java
    client/
      ClientEventHandler.java
  src/main/resources/
    mcmod.info
    InputMethodBlocker-Natives-x86.dll
    InputMethodBlocker-Natives-x64.dll
    assets/inputmethodblocker_gtnh/lang/en_US.lang
```

说明：

- 包名和 `modId` 在实现时统一确定，但整体结构按 GTNH 标准工程组织
- DLL 继续随资源打包分发
- `compat`、`focus`、`ime` 三层保持明确边界，避免逻辑缠绕

## 原生层设计

由于用户确认“没有 DLL 源码，但可以继续分发旧 DLL”，原生层采用“保留旧 DLL、重写 Java 桥接”的策略。

### 资源与加载策略

- 继续在资源目录中打包 `InputMethodBlocker-Natives-x86.dll`
- 继续在资源目录中打包 `InputMethodBlocker-Natives-x64.dll`
- 运行时从 classpath 解包到临时文件
- 使用 `System.load(...)` 加载解包后的 DLL

### 平台与位数判定

- 仅在 Windows 平台尝试启用功能
- 非 Windows 平台直接标记为 unavailable
- 位数优先依据 JVM/系统属性做稳妥判断，而不是仅凭旧版简单字符串判断

### 失败保护

- 资源缺失时只记日志，不崩溃
- DLL 加载失败时只记日志，不崩溃
- native 调用抛错时只记日志并将 bridge 降级为 unavailable

### Java 接口

Java 层只暴露最小接口：

- `initialize()`
- `isAvailable()`
- `setImeActive(boolean active)`

这样后续即使需要更换 native 方案，也不影响上层探测逻辑。

## 原版 GUI 支持设计

原版部分不再复刻旧版那种“每个 GUI 都写一个独立事件处理器”的结构，而是走统一探测：

- 获取当前 `GuiScreen`
- 通过反射或字段扫描识别其中的 `GuiTextField`
- 判断是否存在处于焦点状态的文本框
- 若存在，则视为“应启用输入法”

这个策略天然覆盖：

- `GuiChat`
- `GuiRepair`
- `GuiEditSign`
- `GuiScreenBook`
- 原版多人服务器输入相关界面
- 世界重命名等使用 `GuiTextField` 的场景

相比旧版，新的好处是：

- 少写大量特判
- 不依赖单个字段名时也能退而扫描字段类型
- 适配范围更广

## MUI1 自动支持设计

MUI1 指基于 `com.gtnewhorizons.modularui` 的 UI 体系。

设计目标：

- 自动覆盖所有使用 MUI1 `TextFieldWidget` 或同类文本输入控件的界面
- 不为每个模组单独写兼容逻辑

实现策略：

- 在当前打开的 MUI1 界面中，扫描窗口树或部件树
- 查找 `TextFieldWidget` 或其上游文本输入控件基类
- 读取其聚焦/激活/可输入状态
- 只要存在至少一个处于焦点状态的文本输入控件，就判定应启用输入法

为了兼顾稳定性：

- 优先使用公共类型和可访问方法
- 当公共 API 不足时，使用反射读取必要状态
- 如果发现某版本字段名或结构变化，记录兼容日志并安全失败

## MUI2 自动支持设计

MUI2 指基于 `com.cleanroommc.modularui` 的 UI 体系。

设计目标与 MUI1 相同：

- 自动覆盖所有使用 MUI2 文本输入控件的界面
- 不维护模组白名单

实现策略：

- 扫描当前 MUI2 界面的面板/控件树
- 定位 `TextFieldWidget` 及其同类文本输入控件
- 判断焦点状态
- 输出统一的“当前是否应启用输入法”结果

由于 MUI2 的结构与 MUI1 不同，因此保持独立探测器实现，不试图共用脆弱的内部细节。

## 探测驱动方式

为了兼顾兼容性与实现难度，焦点探测使用“客户端事件驱动 + 当前屏幕状态扫描”的组合方式。

推荐行为：

- 在 GUI 打开、关闭或客户端 tick 时刷新一次焦点状态
- 由 `InputFocusService` 汇总所有 detector 的结果
- 当目标状态从 `false -> true` 时调用 `setImeActive(true)`
- 当目标状态从 `true -> false` 时调用 `setImeActive(false)`

这样可以避免：

- 完全依赖某个 GUI 生命周期 hook
- 因单一框架版本差异导致整个逻辑失效

## 非 Windows 与异常降级策略

本模组是典型的 Windows 客户端功能模组，因此必须明确降级策略：

- 非 Windows：模组可加载，但功能自动关闭
- Windows 但 DLL 缺失：模组可加载，但功能自动关闭
- Windows 但 DLL 加载失败：模组可加载，但功能自动关闭
- 某个兼容探测器失败：记录日志，其他探测器继续工作

降级目标是：

- 不让客户端因为本模组崩溃
- 出问题时尽可能留下足够日志供排障

## 日志策略

建议至少记录以下关键信息：

- 平台检测结果
- JVM/系统位数检测结果
- 选择并加载的 DLL 名称
- DLL 加载是否成功
- MUI1/MUI2 探测器是否启用
- 焦点探测是否因为反射/结构变化失败

日志级别建议：

- 正常初始化信息用 `info`
- 兼容失败和降级信息用 `warn`
- 不可恢复的初始化异常用 `error`

## 兼容边界

本设计明确支持：

- Minecraft 1.7.10 GTNH 客户端
- Java 17-25 环境
- Windows 客户端
- 原版 `GuiTextField`
- 所有使用 MUI1 的通用文本输入控件
- 所有使用 MUI2 的通用文本输入控件

本设计不承诺支持：

- 非 Windows 平台的输入法控制
- 不经过原版/MUI1/MUI2 文本控件体系的自定义 GUI
- 没有文本焦点概念、仅自绘输入的特殊界面

## 测试与验证目标

实现完成后，至少要验证以下结果：

### 构建验证

- 新项目可以在 GTNH 标准构建链下成功编译
- Java 17 开发环境下可以正常运行开发客户端

### 功能验证

- 模组加载后不会因 native 初始化失败导致客户端崩溃
- 聊天框聚焦时启用输入法
- 聊天框关闭后恢复关闭输入法
- 原版铁砧重命名、告示牌、书本等文本场景可用
- 常见 MUI1 文本输入框聚焦时启用输入法
- 常见 MUI2 文本输入框聚焦时启用输入法
- 失焦、切屏或关闭 GUI 时恢复关闭输入法

### 降级验证

- 非 Windows 平台不崩溃
- 缺少 DLL 时不崩溃
- MUI1 或 MUI2 某一层探测失败时，其余层仍可工作

## 后续实现原则

- 先建立 GTNH 标准工程骨架
- 再引入最小可工作的 DLL bridge
- 再实现原版 GUI 探测
- 再补 MUI1、MUI2 通用探测
- 每一层都应有独立验证点

实现时遵循：

- 优先保证稳定和降级安全
- 优先做通用探测，而不是维护兼容名单
- 避免在主类中堆积复杂逻辑

## 自检结论

本设计已完成以下自检：

- 已覆盖用户确认的关键约束：GTNH 新工程、Java 17-25、继续分发旧 DLL、通用支持 MUI1/MUI2
- 未包含 “TODO” / “TBD” / 模糊占位项
- 已明确支持范围、降级边界、模块划分和验证目标
- 项目仍聚焦于单一子目标，没有扩展成额外子系统

当前设计已可进入实现计划编写阶段，但在进入实现计划前，需要先由用户审阅本 spec。
