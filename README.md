# 森罗物语：酒馆 · NeoForge 26.1.2 社区移植版

这是 **森罗物语：酒馆（Kaleidoscope Tavern）的社区非官方移植版本**，将上游 `1.2.0` 的酿造、调酒和酒馆装饰内容带到 Minecraft `26.1.2` / NeoForge，沿用模组 ID `kaleidoscope_tavern`。

## 原项目与许可证

- **源项目**：[KaleidoscopeMods / KaleidoscopeTavern](https://github.com/KaleidoscopeMods/KaleidoscopeTavern)。
- **原作者**：ysbbbbbb、tartaric_acid，以及原项目贡献者。
- **版权署名**：Kaleidoscope Official Production Team。
- **代码许可证**：[BSD-3-Clause](LICENSE-CODE)。
- **美术、音效等资源许可证**：[CC BY-NC-SA 4.0（署名—非商业性使用—相同方式共享）](LICENSE-ASSETS)。

原项目的代码、资源和作者署名随移植保留。两份许可证及来源说明 [NOTICE](NOTICE) 均收录于运行 jar 和源码 jar 的 `META-INF/`。

## 本次移植内容

以原仓库 `main` 分支的 `1.2.0` 为内容基准，结合 `1.21.1` 分支的 NeoForge 实现和 `26.1.2` 分支的平台适配，移植全部 **159 个方块、157 个物品、12 个效果、156 个配方和 37 份酒效果定义**。

自有注册 ID 和全部配方 ID 以 `main` 上游原版为准。

主要内容包括：

- **酿造与调酒**：完整酿造流程、雪克杯、鸡尾酒及自定义鸡尾酒，适配酒桶容器消耗、产物提取和饮用效果，补齐杯装酒的饮用音效。
- **酒馆装饰**：酒架、窖藏酒柜、香薰、垂灯、灯串，以及原版瓶装物品的放置功能。
- **新版平台适配**：更新存档与物品数据、网络和配方同步、模型、渲染、粒子、界面及动画接口，保留冒险模式交互修正。
- **模组兼容**：更新 JEI、REI、Jade、Curios 和 Ponder 的对应功能，补充 Catnip 的命令参数注册适配。
- **原作资源**：保留上游纹理、音效、教学结构及已有翻译。

## 运行与构建

运行环境为 **Minecraft 26.1.2、NeoForge 26.1.2.126、Java 25**。将模组 jar 放入客户端或服务端的 `mods` 目录即可加载。

可选搭配 JEI 或 REI 查看配方、Jade 查看方块信息、Curios 使用灯串饰品、Ponder 查看教学。EMI 兼容待目标版本 API 接入。

本地构建：

```sh
./gradlew build
```

Windows 使用 `.\gradlew.bat build`。产物位于：

```text
build/libs/kaleidoscopetavern-1.2.0-neoforge+mc26.1.2.jar
```

详细测试范围、验证结果、开发环境版本和复现命令见 [测试与验证](TESTING.md)。
