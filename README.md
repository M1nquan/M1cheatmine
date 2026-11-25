# M1CheatMine — AeroGlass

Minecraft Forge client mod for **1.21.10** built on **Java 21**, featuring an **AeroGlass** `ClickGuiScreen`, draggable HUD modules, and block/chunk filtering for **X-Ray** via Mixin.

## Key Features
* **ClickGUI (`AeroGlass`)**: Compact, draggable overlay with tabbed navigation (including a dedicated **HUD** tab) and text clipping/layout handling.
* **HUD Modules**: Modular overlay widgets (`Watermark`, `ArrayList`, `FPS`, `CPS`, `Keystrokes`, etc.) managed through `HudModule` and `ModuleManager`.
* **X-Ray (`XrayModule` + Mixin)**: Boosts gamma and filters rendered block faces (`Block.shouldRenderFace` / block mixins) so target ores, chests, and spawners stand out while rebuilding chunks cleanly on toggle.
* **Combat, Movement & Misc Modules**: Organized under `com.m1cheatmine.module.impl.*` (`KillAura`, `TriggerBot`, `Velocity`, `Fly`, `ElytraFly`, `NoFall`, `Blink`, `PacketFly`, and more).

## Compatibility Warning — Do NOT Use OptiFine
* **Avoid OptiFine** with this mod on Minecraft 1.21.10.
* OptiFine conflicts with HUD rendering (such as `NoSuchMethodError` in layered/contextual GUI rendering) and alters block/chunk rendering pipelines that Mixins hook into.

## Build & Setup
1. Ensure **Java 21** is installed and configured for your Forge 1.21.10 Gradle environment.
2. Build the mod jar from the project root:
   ```bash
   ./gradlew clean build
   ```
   *(On Windows Command Prompt: `.\gradlew.bat clean build`)*
3. Copy the built `.jar` from `build/libs/` into your Minecraft Forge `mods/` folder (without OptiFine).

## Repository Notes
* Keep `gradle-bin/`, `gradle-bin.zip`, `.gradle/`, `build/`, and `*.jar` ignored via `.gitignore` so you stay well under GitHub's 100 MB per-file limit (`GH001`).
