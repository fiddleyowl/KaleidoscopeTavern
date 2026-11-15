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

本次以原仓库 `main` 分支的 `1.2.0` 为完整内容基准，结合 `1.21.1` 分支的 NeoForge 实现和 `26.1.2` 分支的平台适配：

| 用途 | 上游提交 |
|---|---|
| 完整内容、美术和模型修正 | [`main · c4ec188`](https://github.com/KaleidoscopeMods/KaleidoscopeTavern/commit/c4ec1880bd44cf3139d3ba744ab30bb379cf1416) |
| NeoForge 实现参考 | [`1.21.1 · a1afba3`](https://github.com/KaleidoscopeMods/KaleidoscopeTavern/commit/a1afba34981a00e89d57130d3dc8f1e64f1820a2) |
| 26.1.2 平台适配参考 | [`26.1.2 · 9b8f165`](https://github.com/KaleidoscopeMods/KaleidoscopeTavern/commit/9b8f165a4641e738e694641dd81240a0206573e1) |

移植包含 **159 个方块、157 个物品、12 个效果、156 个配方和 37 份酒效果定义**，包括雪克杯、鸡尾酒、酒架、窖藏酒柜、香薰、垂灯，以及原版瓶装物品的放置功能。

主要改动包括：

- **构建与平台接口**：迁移至 Java 25、NeoForge 26.1.2 和 ModDevGradle，更新注册、事件、网络消息及物品交互接口，保留目标分支的冒险模式交互修正。
- **酿造与调酒**：适配配方读取和同步，迁移酒桶的容器消耗与产物提取逻辑，接入新版即时药水效果调用。
- **物品与存档数据**：将鸡尾酒颜色、效果和雪克杯内容迁移至物品数据组件，更新方块实体的保存与读取；酒效果改用客户端、服务端同步的数据包注册表。
- **客户端表现**：适配新版渲染状态、物品模型入口、颜色、粒子、界面及动画接口，更新雪克杯和灯串渲染。
- **模组兼容**：更新 JEI、REI、Jade、Curios 和 Ponder 接口，补充 Catnip `1.0.52+mc26.1.2` 缺失的命令参数注册。
- **资源与开发流程**：保留上游纹理、音效、教学结构及已有翻译，整理数据生成流程，加入内容比对、服务端回归、客户端自动检查及 CI 构建。

## 运行与构建

验证环境为 **Minecraft 26.1.2、NeoForge 26.1.2.126、Java 25**。将模组 jar 放入客户端或服务端的 `mods` 目录即可加载；下列兼容模组按需安装。

项目使用 Gradle wrapper `9.9.0` 和 ModDevGradle `2.0.148`：

```sh
./gradlew build
```

Windows 使用 `.\gradlew.bat build`。产物位于：

```text
build/libs/kaleidoscopetavern-1.2.0-neoforge+mc26.1.2.jar
```

## 可选模组兼容

| 模组 | 开发环境版本 | 对应功能 |
|---|---|---|
| JEI | `29.53.0.115` | 酿造、压榨和调酒配方分类 |
| REI | `26.1.819+neoforge` | 酿造、压榨和调酒配方分类；配套 Architectury `20.1.17`、Cloth Config `26.1.154` |
| Jade | `26.1.12+neoforge` | 酒桶信息、药水瓶图标 |
| Curios | `15.0.0+26.1.2` | 灯串饰品识别与渲染 |
| Ponder | `1.0.52+mc26.1.2` | 葡萄种植、压榨和酿造教学；配套 Catnip、Flywheel |

EMI 的兼容源码已保留，等待 NeoForge 26.1.2 API 发布后接入。Carry On 的黑名单消息已保留，目标版本联调待补充。

默认开发客户端加载 JEI、Jade、Curios 和 Ponder，也可以切换为 REI 或仅运行酒馆本体：

```sh
./gradlew runClient
./gradlew runClient -PrecipeViewer=rei
./gradlew runClient -PwithCompat=false -PrecipeViewer=none
```

## 测试与验证

本次已通过数据生成、上游内容比对、服务端 GameTest、三种客户端环境检查和最终构建。

### 上游内容比对

[`scripts/verify_port.py`](scripts/verify_port.py) 直接读取固定上游提交，检查：

- 全部方块、物品和效果的注册 ID。
- 156 个配方和 37 份酒效果定义的内容对应关系。
- 329 份原有纹理、音效、结构及语言资源，其中已有翻译的键和值完整保留。
- 1,106 份模型、方块状态和新版物品定义文件，以及模组自身的模型、纹理引用。

### 服务端回归

[`src/gametest/`](src/gametest/) 中包含四项项目测试：

1. **内容与存档**：检查注册数量、方块状态属性及所有方块实体的保存和加载，覆盖储物、酒柜、药水瓶、雪克杯和鸡尾酒数据。
2. **调酒与物品组件**：检查 12 个调酒配方的输入匹配和产出，以及鸡尾酒颜色、效果、雪克杯库存和结果的序列化与反序列化。
3. **掉落物容器**：检查酒桶消耗掉落物容器、输出非方块物品，并在取空后重置。
4. **已放置容器**：检查酒桶消耗已放置的容器、输出非方块物品，并在取空后重置。

四项项目测试与一项原版测试均已通过，共 **5 项**。

### 客户端检查

分别在 **JEI 环境、REI 环境和仅运行酒馆本体的环境** 中启动游戏、创建存档并进入世界，检查：

- 放置全部 159 个方块，运行雪克杯、灯串和粒子的客户端渲染路径。
- 解析全部 157 个物品在各显示上下文中的模型。
- 客户端收到全部 12 个调酒配方。
- JEI、REI 的酿造、压榨、调酒分类数量与已同步配方一致。

### 构建产物检查

运行 jar 和源码 jar 均已核对许可证、来源说明、资源数量及测试文件排除情况；运行 jar 的 Java 字节码版本为 Java 25。

后续人工游玩验证将覆盖各类动画的完整表现、Ponder 教学场景和多人交互。

### 复现命令

```sh
./gradlew runData
python3 scripts/verify_port.py
./gradlew runGameTestServer
./gradlew build
```

内容比对脚本需要本地 Git 历史包含上述上游提交。客户端自动检查命令为：

```sh
./gradlew runClient -PclientSmoke=true
./gradlew runClient -PclientSmoke=true -PrecipeViewer=rei
./gradlew runClient -PclientSmoke=true -PwithCompat=false -PrecipeViewer=none
```

客户端检查会创建 `_kaleidoscope_port_smoke_*` 开发存档，并在完成后自动退出。测试源码和测试配方仅在验证时加载，发布 jar 包含正式模组内容。

## 资源与数据目录

- `src/main/resources/assets/`：上游方块状态、模型、美术、音效与教学资源。
- `src/generated/resources/`：数据生成器输出的物品模型入口、配方、战利品、标签、粒子、音效定义和酒效果数据；JSON 沿用仓库的 CRLF 规则。
- `src/gametest/`：开发专用的服务端与客户端验证代码、测试数据。

酒效果数据路径为 `data/<命名空间>/kaleidoscope_tavern/drink_effect/<名称>.json`。酿造等级使用 `"1"` 至 `"6"` 的键，无酿造等级的鸡尾酒使用 `"0"`。
