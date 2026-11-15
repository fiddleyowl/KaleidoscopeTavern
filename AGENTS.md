# AGENTS.md

## LANGUAGE POLICY

- 默认使用简体中文回答。
- 除非我明确要求英文，否则不要切换英文叙述。
- 代码、命令、报错、API 名称保持原文，不要强行翻译。
- 提问澄清时也使用中文。

## Project facts that matter

- NeoForge mod for Minecraft `26.1.2` / NeoForge `26.1.2.126`; use Java 25. ModDevGradle is `2.0.148`. Gradle wrapper is pinned in
  `gradle/wrapper/gradle-wrapper.properties`, so prefer `./gradlew` in CI/Unix docs and `.\gradlew.bat` on Windows.
- Mod id is `kaleidoscope_tavern`; Java package root is `src/main/java/com/github/ysbbbbbb/kaleidoscopetavern/`.
- Main mod entrypoint is `KaleidoscopeTavern.java`. It registers the DeferredRegisters from `init/*` on the mod event
  bus and exposes `KaleidoscopeTavern.modLoc(...)` for resource ids.

## Commands

- Full local verification: `.\gradlew.bat build` (`./gradlew build` in GitHub Actions).
- List available NeoForge/Gradle tasks: `.\gradlew.bat tasks --all`.
- Launch local runs: `.\gradlew.bat runClient`, `.\gradlew.bat runClient2`, `.\gradlew.bat runServer`,
  `.\gradlew.bat runGameTestServer`.
- Regenerate data/assets: `.\gradlew.bat runData`; this writes to `src/generated/resources/` using `src/main/resources/`
  as existing inputs.
- Import the Gradle project in the IDE; ModDevGradle manages development runs.
- Server regression tests live in `src/gametest/` and run with `./gradlew runGameTestServer`; they are excluded from published jars.
- Content parity is checked with `python3 scripts/verify_port.py` against upstream `c4ec188`.
- `./gradlew runClient -PclientSmoke=true` creates a separate development test world, checks item models, and exits.
- Runtime compatibility variants: `-PrecipeViewer=rei`, or `-PwithCompat=false -PrecipeViewer=none`.

## Generated resources and data

- `src/generated/resources` is part of `sourceSets.main.resources`; `.cache/**` is excluded. Do not treat generated
  resources as disposable unless rerunning `runData` and reviewing the diff.
- Datagen is wired in `datagen/DataGenerators.java` via `GatherDataEvent.Client`. Providers cover native item definitions,
  recipes, loot tables, synced drink-effect registry data, particles, sounds, and tags.
- Latest upstream blockstates and artwork models are static inputs under `src/main/resources/assets/`; preserve them when regenerating data.
- `processResources` expands Gradle properties into `META-INF/neoforge.mods.toml`; edit placeholders with the
  Gradle properties in mind.

## Code organization quirks

- Registry declarations live under `init/`: blocks/block entities in `ModBlocks`, items in `ModItems`,
  entities/effects/sounds/particles/tree decorators/creative tabs in their matching `Mod*` files.
- Blocks, items, fluids, components, effects and recipe serializers/types use DeferredRegisters.
- `ModDatapackRegistries` registers the synced drink-effect registry through `DataPackRegistryEvent.NewRegistry`.
- NeoForge selects subscriber buses from event types. Do not restore removed `EventBusSubscriber.Bus` arguments.
- Client rendering extracts immutable state before submission; do not retain block entities inside render states.
- Common setup side effects are centralized in `init/register/CommonRegistry.java` and use
  `FMLCommonSetupEvent.enqueueWork(...)` for network, composter, tap, and dispenser registrations.
- Network payloads are registered in `network/NetworkHandler.java` using `RegisterPayloadHandlersEvent`.
- Recipe transfer uses `DatapackSyncEvent` and `ClientRecipeEvent`, including the shaker recipe type.
- Optional Catnip argument registration fixes are confined to `1.0.52+mc26.1.2`.


## Client and compat entrypoints

- Client-only setup/events live under `client/` and should be guarded with `Dist.CLIENT` where loaded by event
  subscribers.
- EMI sources are retained but excluded until a 26.1.2 API exists. Other recipe-viewer integrations have explicit plugin entrypoints: JEI `compat/jei/ModJeiPlugin.java`, REI
  `compat/rei/ModREIClientPlugin.java`, EMI `compat/emi/ModEmiPlugin.java`.

## Workflow gotchas

- `run/` is ignored local runtime state. It may contain KubeJS scripts/logs and per-run game directories; do not commit
  or rely on it as source.
- CI workflow `.github/workflows/gradle-publish-26.1.2.yml` builds on pushes to `26.1.2-new` touching `src/main/**`.
  Manual `workflow_dispatch` runs also publish `build/libs/*.jar` as a prerelease.
- `.gitattributes` forces `*.json` to CRLF; avoid normalizing JSON line endings casually.
