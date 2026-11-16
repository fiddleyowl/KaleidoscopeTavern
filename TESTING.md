# 测试与验证

本文说明 NeoForge 26.1.2 移植版的改动、回归测试覆盖范围、验证结果与复现方法。项目介绍、作者与许可证见 [README](README.md)。

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

## 移植改动

### 平台与行为适配

- **平台接口**：更新存档与物品数据、网络和配方同步、模型、渲染、粒子、界面及动画接口，适配 Minecraft 26.1.2 / NeoForge。
- **酿造与饮用**：适配酒桶的容器消耗、产物提取与取空重置；补齐杯装酒饮用过程和结束时的音效，保留饮用效果与空杯返还。
- **灯串装备**：添加新版 `EQUIPPABLE` 组件，使全部 17 种灯串可放入胸甲槽，同时保留右键放置功能。
- **酒瓶交互**：空手交互回退检查主手是否为空，持方块点击时正常放置方块；取瓶交互遵守冒险模式限制。
- **压榨桶自动化**：恢复无方向物品接口查询，保留侧面输入及上下方向限制。
- **原料返还**：背包无法容纳全部原料时，将剩余部分掉落在玩家身边，避免取出原料后丢失。
- **可选模组兼容**：适配 JEI、REI、Jade、Curios 和 Ponder，补充指定 Catnip 版本的命令参数注册。
- **JEI 分类名称**：酒桶和果盆分类标题使用原作的 `block.kaleidoscope_tavern.*` 翻译键。

### 名称与整合包兼容

命名以 `main@c4ec188` 为基准。模组 ID 为 `kaleidoscope_tavern`；注册名称、自有标签、配置键、现有翻译键和值保持上游内容。展板与挂画物品分别使用上游共享名称 `block.kaleidoscope_tavern.sandwich_board`、`block.kaleidoscope_tavern.painting`。

物品数据组件沿用上游 NeoForge 分支的 `brew_level`、`shaker_result`、`signature_cocktail_effects` 和 `signature_cocktail_color`，均位于 `kaleidoscope_tavern` 命名空间。酒瓶的颜色提示与调酒最低 4 级品质要求遵循 `main`。

草展板配方保留原始 ID `kaleidoscope_tavern:grass_sandwich_board_from_grass`，原料使用新版物品 `minecraft:short_grass`。对应解锁进度也沿用原始 ID；不额外生成 `from_short_grass` 配方。

同时提供 `forge:fruits/grapes` 和新版 `c:fruits/grapes` 标签。旧标签映射到新版标签，使用旧名称查询葡萄的整合包可以读取新版公共标签的内容。

数据包遵循 26.1.2 的目录与 JSON 规范：例如 `recipes` 改为 `recipe`、`tags/items` 改为 `tags/item`。酒效果使用同步注册表路径，详见本文末尾的数据目录说明。兼容范围为 26.1.2 下的命名、ID 和整合包接口。

## 验证范围与结果

数据生成、上游内容比对、服务端 GameTest、三种客户端环境检查和构建均通过。

### 上游内容比对

[`scripts/verify_port.py`](scripts/verify_port.py) 直接读取固定上游提交，检查：

- 全部方块、物品、方块实体、实体、效果、流体、流体类型、粒子、音效、创造模式标签页、树装饰器和配方类型的注册 ID。
- 全部自有标签、数据组件、Java 包路径、配置键和已有 NeoForge 消息 ID。
- Java 代码引用的翻译键在中文、英文语言文件中存在，动态翻译键族可解析。
- 156 个配方及对应解锁进度的 ID 集合与上游完全一致，核对配方内容和 37 份酒效果定义。
- 1,783 个上游资源路径，按新版目录规范映射后全部存在。
- 329 份原有纹理、音效、结构及语言资源，其中已有翻译的键和值完整保留。
- 1,106 份模型、方块状态和新版物品定义文件，以及模组自身的模型、纹理引用。

### 服务端回归

[`src/gametest/`](src/gametest/) 中包含十五项项目测试：

1. **内容与存档**：检查注册数量、方块状态属性及所有方块实体的保存和加载，覆盖储物、酒柜、药水瓶、雪克杯和鸡尾酒数据。
2. **调酒与物品组件**：检查 12 个调酒配方的输入匹配和产出，以及鸡尾酒颜色、效果、雪克杯库存和结果的序列化与反序列化。
3. **掉落物容器**：检查酒桶消耗掉落物容器、输出非方块物品，并在取空后重置。
4. **已放置容器**：检查酒桶消耗已放置的容器、输出非方块物品，并在取空后重置。
5. **杯装酒饮用**：逐一检查全部 14 种杯装鸡尾酒的饮用过程及结束音效调用、32 tick 使用时长和空杯返还，覆盖 Allium Garden 与自定义鸡尾酒。
6. **灯串装备**：通过玩家物品栏的实际装备槽，检查全部灯串可放入胸甲槽、不能放入头盔槽，并保留右键放置功能。
7. **压榨桶自动化与装桶**：检查侧面、上下和无方向的物品/流体接口查询，以及 8 个葡萄压榨得到 1,000 mB 果汁、用空桶取出后清空流体的过程。
8. **饮用容器与解毒**：检查全部瓶装酒、果汁桶在单件和两件堆叠时的消耗与容器返还，并检查果汁桶解除中毒。
9. **雪克杯时序**：检查 18、19、68、69、88、89、98、99、111 tick 释放时的调酒结果，包括 Allium Garden 的配方匹配。
10. **效果属性**：检查长臂的方块/实体交互距离、高跟鞋的跨步高度增量，以及移除效果后属性恢复。
11. **酒瓶点击交互**：检查持方块点击酒瓶时正常放置方块、空手点击时取回酒瓶，以及冒险模式限制。
12. **名称与公开 ID**：检查 157 个物品和 159 个方块的实际名称翻译键、上游原始配方 ID、葡萄标签入口、4 级调酒品质门槛和酒瓶颜色提示。
13. **原料返还与背包容量**：检查取出 8 个葡萄时，背包可容纳 8 个、4 个和 0 个的情况，核对装入数量与掉落数量之和。
14. **燃料与堆肥**：通过熔炉燃料查询检查葡萄藤燃烧时间为 200 tick，通过堆肥桶实际使用的查询检查葡萄藤与四种葡萄的堆肥概率。
15. **网络编解码**：检查瓶装酒、自定义鸡尾酒、雪克杯库存和结果的完整组件数据，以及三类配方的原料、产出、流体、时长和颜色信息；检查文字编辑、打开界面与清空雪克杯消息的编码和解码。

十五项项目测试与一项原版测试均已通过，共 **16 项**。

### 客户端检查

分别在 **JEI 环境、REI 环境和仅运行酒馆本体的环境** 中启动游戏、创建存档并进入世界，检查：

- 放置全部 159 个方块，运行雪克杯、灯串和粒子的客户端渲染路径。
- 解析全部 157 个物品在各显示上下文中的模型。
- 检查全部 157 个物品的客户端翻译键可解析，实际显示名称与翻译一致。
- 检查物品完整提示、1 至 6 级瓶装酒的提示，以及效果、实体、流体和创造模式标签页名称的翻译键；递归检查提示中的嵌套文字。
- 客户端收到全部 12 个调酒配方。
- JEI、REI 的酿造、压榨、调酒分类数量与已同步配方一致。
- JEI、REI 的三个配方分类标题均有可解析的翻译键，中文显示为“酒桶”“果盆”“雪克杯”。

人工游戏内检查确认 Allium Garden 的饮用音效正常。

### 构建产物检查

运行 jar 和源码 jar 均已核对许可证、来源说明、资源数量及测试文件排除情况；运行 jar 的 Java 字节码版本为 Java 25。

### 尚未完成的人工验证

各类动画的完整表现、Ponder 教学场景、Curios 灯串，以及多人环境中的调酒、牌子改字、重连和数据包重载。

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

Gradle 显式使用小写的 `src/gametest/` 目录，适用于 macOS 和区分文件名大小写的 Linux CI。

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
