![构建](https://github.com/android/platform-samples/actions/workflows/build.yml/badge.svg)

# Android 平台示例

本仓库包含一系列示例，展示了不同 Android 操作系统平台 API 的使用。示例按主题组织到文件夹中，每个文件夹包含一个 README 文件，提供有关该文件夹中示例的更多信息。

> **注意：** 这些示例旨在独立展示特定功能，可能使用了简化的代码。它们并不适用于生产环境代码。项目使用 [casa-android](https://github.com/google/casa-android)（仅用于演示项目）。有关最佳实践，请遵循我们的文档并查看 [Now In Android](https://github.com/android/nowinandroid)。

浏览每个主题示例文件夹中的示例：

- [无障碍（Accessibility）](https://github.com/android/platform-samples/tree/main/samples/accessibility)
- [相机（Camera）](https://github.com/android/platform-samples/tree/main/samples/camera)
- [连接（Connectivity）](https://github.com/android/platform-samples/tree/main/samples/connectivity)
- [图形（Graphics）](https://github.com/android/platform-samples/tree/main/samples/graphics)
- [位置（Location）](https://github.com/android/platform-samples/tree/main/samples/location)
- [隐私（Privacy）](https://github.com/android/platform-samples/tree/main/samples/privacy)
- [存储（Storage）](https://github.com/android/platform-samples/tree/main/samples/storage)
- [用户界面（User-interface）](https://github.com/android/platform-samples/tree/main/samples/user-interface)
- 更多内容即将推出...

我们不断向该仓库添加新的示例。你可以在[这里](https://github.com/android/platform-samples/tree/main/samples/README.md)找到所有可用示例的列表。

> 🚧 **进行中的工作：** 我们正在努力将更多现有和新示例引入这种格式。

## 如何运行

1. 克隆仓库
2. 在 Android Studio 中打开整个项目。
3. 同步并运行 `app` 配置

应用将打开示例列表屏幕，允许你浏览不同类别和可用示例。

> **注意：** `app` 模块用于汇总所有示例，但与其功能无关，可以忽略它。底层实现已完成，不需要理解任何示例功能。

### 深链接到示例

要直接打开特定示例，可以使用自动生成的配置之一。

1. 至少构建项目一次
2. 打开 `Run Configuration` 下拉菜单
3. 选择示例名称
4. 运行

> **提示：** 使用 `⌃⌥R` 或 `Alt+Shift+F10` 快捷键打开完整列表并启动所选项。

## 报告问题

你可以使用[此仓库报告示例问题](https://github.com/android/platform-samples/issues)。在这样做时，请确保指定你所指的示例。

## 贡献

请贡献！我们将乐于审核任何拉取请求。在此之前，请务必先阅读[贡献](CONTRIBUTING.md)页面。

> 注意：在提交拉取请求之前，请确保运行 `./gradlew --init-script gradle/spotless-init.gradle.kts spotlessApply`。

## 许可证

```
版权所有 2023 The Android Open Source Project
 
根据 Apache 许可证 2.0 版（“许可证”）授权，除非遵守许可证，否则你不得使用此文件。你可以在以下网址获得许可证副本：

    https://www.apache.org/licenses/LICENSE-2.0

除非适用法律要求或书面同意，按许可证分发的软件按“原样”分发，不附带任何明示或暗示的担保或条件。请参阅许可证了解管理权限和限制的具体语言。
```