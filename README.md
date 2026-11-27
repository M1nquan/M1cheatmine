# M1CheatMine (Aero Glass)

Custom utility & PvP client mod for **Minecraft Forge 1.21.11** running on **Java 21** and **Gradle 8.14.3**.

## Tech Stack & Environment

- **Minecraft / Modloader:** Forge `1.21.11`
- **Java:** `Java 21`
- **Build Tool:** `Gradle 8.14.3` (via Gradle Wrapper)
- **Repository:** `https://github.com/M1nquan/M1cheatmine`

## Key Features

- **Aero Glass ClickGUI (`ClickGuiScreen`)**
  - Glassmorphism window with draggable panels, category tabs, search bar, live module counters, and accent color themes (`Aero Blue`, `Cyan`, `Rose`, `Emerald`, `Gold`, `Rainbow`).
  - Custom scaled/spaced text rendering helper and keybind assignment (`Middle Click` on a module card).
  - Draggable HUD overlays (`FpsModule`, `CpsModule`, `KeystrokesModule`, `WatermarkModule`, `ArrayListModule`).
- **Combat Modules**
  - Includes `SpearAuraModule` and `MaceAuraModule` (target filtering, aim assist/rotation, cooldown/charge checks, auto-equip, and swap-back support) alongside core combat modules managed via `ModuleManager`.
- **Render & World Modules**
  - `EspModule`, `HoleEspModule`, `StorageEspModule`, and `TracersModule` using dynamic `RenderType` resolution for `1.21.11` mapping resilience.
- **Mixins**
  - `EntityMixin` (e.g., hitbox scaling hook with `require = 0` safety) and `MouseMixin`.

## Build & Setup

1. Ensure **JDK 21** is installed and configured in your environment.
2. Open a terminal in the project root directory.
3. Build the mod:

   ```bash
   .\gradlew.bat clean build
   ```

   On Linux/macOS:

   ```bash
   ./gradlew clean build
   ```

4. Find the compiled `.jar` output under `build/libs/`.

## Keybindings

- **Open ClickGUI:** `Right Shift` (`key.m1cheatmine.open_gui`)
- **Freelook:** `V` (`key.m1cheatmine.freelook`)
eof

Mình vừa tạo file `README.md` tổng hợp môi trường Forge 1.21.11, tính năng chính, phím tắt và cách build bằng Gradle. Bạn có muốn thêm mục nào khác (như danh sách đầy đủ từng module hoặc hướng dẫn cài đặt vào thư mục `mods`) vào `README.md` không?
