# 测试与验证

本文记录 NeoForge 26.1.2 移植版的验证范围、结果与复现方法。项目介绍、作者与许可证见 [README](README.md)。

## 环境

| 项目 | 版本 |
|---|---|
| Minecraft | `26.1.2` |
| NeoForge | `26.1.2.126` |
| Java | `25` |
| Gradle wrapper | `9.9.0` |
| ModDevGradle | `2.0.148` |

默认开发客户端加载 JEI、Jade、Curios 和 Ponder。兼容模组版本如下：

| 模组 | 开发环境版本 | 对应功能 |
|---|---|---|
| JEI | `29.53.0.115` | 酿造、压榨和调酒配方分类 |
| REI | `26.1.819+neoforge` | 酿造、压榨和调酒配方分类；配套 Architectury `20.1.17`、Cloth Config `26.1.154` |
| Jade | `26.1.12+neoforge` | 酒桶信息、药水瓶图标 |
| Curios | `15.0.0+26.1.2` | 灯串饰品识别与渲染 |
| Ponder | `1.0.52+mc26.1.2` | 葡萄种植、压榨和酿造教学；配套 Catnip、Flywheel |

EMI 的兼容源码已保留，等待 NeoForge 26.1.2 API 发布后接入。Carry On 的黑名单消息已保留，目标版本联调待补充。

## 内容来源与比对基准

| 用途 | 上游提交 |
|---|---|
| 完整内容、美术和模型修正 | [`main · c4ec188`](https://github.com/KaleidoscopeMods/KaleidoscopeTavern/commit/c4ec1880bd44cf3139d3ba744ab30bb379cf1416) |
| NeoForge 实现参考 | [`1.21.1 · a1afba3`](https://github.com/KaleidoscopeMods/KaleidoscopeTavern/commit/a1afba34981a00e89d57130d3dc8f1e64f1820a2) |
| 26.1.2 平台适配参考 | [`26.1.2 · 9b8f165`](https://github.com/KaleidoscopeMods/KaleidoscopeTavern/commit/9b8f165a4641e738e694641dd81240a0206573e1) |

## 验证范围与结果

本次已通过数据生成、上游内容比对、服务端 GameTest、三种客户端环境检查和最终构建。

### 上游内容比对

[`scripts/verify_port.py`](scripts/verify_port.py) 直接读取固定上游提交，检查：

- 全部方块、物品和效果的注册 ID。
- 156 个配方和 37 份酒效果定义的内容对应关系。
- 329 份原有纹理、音效、结构及语言资源，其中已有翻译的键和值完整保留。
- 1,106 份模型、方块状态和新版物品定义文件，以及模组自身的模型、纹理引用。

### 服务端回归

[`src/gametest/`](src/gametest/) 中包含五项项目测试：

1. **内容与存档**：检查注册数量、方块状态属性及所有方块实体的保存和加载，覆盖储物、酒柜、药水瓶、雪克杯和鸡尾酒数据。
2. **调酒与物品组件**：检查 12 个调酒配方的输入匹配和产出，以及鸡尾酒颜色、效果、雪克杯库存和结果的序列化与反序列化。
3. **掉落物容器**：检查酒桶消耗掉落物容器、输出非方块物品，并在取空后重置。
4. **已放置容器**：检查酒桶消耗已放置的容器、输出非方块物品，并在取空后重置。
5. **杯装酒饮用**：逐一检查全部 14 种杯装鸡尾酒的饮用过程及结束音效调用、32 tick 使用时长和空杯返还，覆盖 Allium Garden 与自定义鸡尾酒。

五项项目测试与一项原版测试均已通过，共 **6 项**。

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

内容比对脚本需要本地 Git 历史包含本文列出的上游提交。客户端自动检查命令为：

```sh
./gradlew runClient -PclientSmoke=true
./gradlew runClient -PclientSmoke=true -PrecipeViewer=rei
./gradlew runClient -PclientSmoke=true -PwithCompat=false -PrecipeViewer=none
```

客户端检查会创建 `_kaleidoscope_port_smoke_*` 开发存档，并在完成后自动退出。测试源码和测试配方仅在验证时加载，发布 jar 包含正式模组内容。

Windows 下将命令中的 `./gradlew` 替换为 `.\gradlew.bat`。

## 开发环境运行

```sh
./gradlew runClient
./gradlew runClient -PrecipeViewer=rei
./gradlew runClient -PwithCompat=false -PrecipeViewer=none
```

## 资源与数据目录

- `src/main/resources/assets/`：上游方块状态、模型、美术、音效与教学资源。
- `src/generated/resources/`：数据生成器输出的物品模型入口、配方、战利品、标签、粒子、音效定义和酒效果数据；JSON 沿用仓库的 CRLF 规则。
- `src/gametest/`：开发专用的服务端与客户端验证代码、测试数据。

酒效果数据路径为 `data/<命名空间>/kaleidoscope_tavern/drink_effect/<名称>.json`。酿造等级使用 `"1"` 至 `"6"` 的键，无酿造等级的鸡尾酒使用 `"0"`。
