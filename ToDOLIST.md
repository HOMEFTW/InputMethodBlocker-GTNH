# TODO 列表

## 当前计划
- [ ] 使用诊断版 jar 在 Windows GTNH 客户端复现问题，并收集 `latest.log` 中 `inputmethodblockergtnh` 相关日志
- [ ] 在 Windows GTNH 客户端验证开始界面不再卡死，并复测进入游戏后的聊天框、原版文本 GUI、MUI1、MUI2 输入框
- [ ] 在 Windows GTNH 客户端验证 Angelica、NEI、ServerUtilities 已加入白名单的搜索框与输入框
- [ ] 为 `ModularUi1TextFieldDetector` 补充首批 MUI1 白名单 screen/field 项
- [ ] 为 `ModularUi2TextFieldDetector` 补充首批 MUI2 白名单 screen/field 项
- [ ] 根据实机测试继续补充 `AeTerminalTextFieldDetector` 的 AE2 / AE2Things 白名单项
- [ ] 根据实机测试继续补充 `ModSearchTextFieldDetector` 的 Angelica、NEI、ServerUtilities 白名单项
- [ ] 排查并修复 `spotlessCheck` 的 Spotless/脚手架异常

## 未来想法
- [ ] 如果旧 DLL 在后续版本上出现兼容问题，可考虑重写原生桥接层
- [ ] 为更多特殊自定义 GUI 增加可选兼容扩展点

## 已完成
- [x] 将除原版白名单外的所有 detector 收敛为按模组/类存在启用的可选兼容层
- [x] 为 Angelica、NEI、ServerUtilities 的首批搜索框与输入框添加白名单支持
- [x] 为 AE2 与 AE2Things 终端搜索框添加首批白名单支持
- [x] 移除遗留的通用反射扫描代码，运行产物中不再包含 `ReflectionWalker`
- [x] 将 MUI1 和 MUI2 检测改为白名单实现，并保留后续按名单扩展的兼容方式
- [x] 将原版输入框检测改为旧模组式白名单实现，停止对任意原版 GUI 的通用扫描
- [x] 修复开始界面因 GUI 对象图反射遍历过深导致的客户端卡死问题
- [x] 修复 `ReflectionWalker` 在真实客户端对象图扫描中的空指针问题，并重新打包发布 jar
- [x] 按 `docs/superpowers/plans/2026-04-19-inputmethodblocker-gtnh-implementation.md` 完成主要编码任务
- [x] 将 `InputMethodBlocker-master` 迁移为新的 `InputMethodBlocker-GTNH` GTNH 模组工程
- [x] 使用 GTNH 现代构建链支持 Java 17-25 环境
- [x] 复用旧版 DLL，并重写 Java 侧 Windows IME bridge
- [x] 实现原版 GUI、MUI1、MUI2 的输入焦点检测基础框架，并后续收敛为白名单实现
- [x] 明确迁移方向为“中度重构 + GTNH 新工程重建”
- [x] 明确继续分发旧版 DLL
- [x] 明确 MUI1 / MUI2 支持目标为“自动通用支持”
- [x] 完成首版设计 spec 编写
- [x] 完成首版实现计划编写

## 暂缓 / 放弃
- 暂无
## 当前计划
- [ ] 按 `docs/superpowers/plans/2026-04-20-native-ime-rewrite-implementation.md` 重写 Windows x64 native IME DLL
- [ ] 在真实 Windows GTNH 客户端验证主菜单、游戏内非输入状态、原版白名单、AE2、Angelica、NEI、ServerUtilities、MUI1、MUI2 的 IME 行为
- [ ] 根据实机结果继续补充 MUI1 / MUI2 与各兼容层白名单条目
- [ ] 排查并修复 `spotlessCheck` 的 Spotless/脚手架异常

## 已完成
- [x] 编写并确认 native 重写设计文档 `docs/superpowers/specs/2026-04-20-native-ime-rewrite-design.md`
- [x] 编写 native 重写实施计划 `docs/superpowers/plans/2026-04-20-native-ime-rewrite-implementation.md`
- [x] 实现 Windows x64 native IME DLL 重写，并重新打包 `inputmethodblockergtnh-0.1.0.jar`

---
