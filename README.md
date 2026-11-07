# Katana Client (Fabric, Minecraft 1.21.11) - made by bluxy62

Client-side HUD and menu. Features:
- **Watermark**: pixel-art katana logo, "KATANA CLIENT", "made by bluxy62"
- **Info panel**: FPS, CPS (left | right), ping to the server, current server address
- **Keystrokes**: W A S D, LMB/RMB with per-button CPS, space bar, smooth press animation
- **Armor status**: your four armor pieces with durability %
- **Themes**: accent color (HSB sliders + validated hex box), rainbow mode, HUD size 50-200%, background opacity, corner placement for each element
- **Freecam**: detached camera (WASD, Space/Shift for up/down, Sprint = 3x speed, speed slider)

Keys: **Right Shift** opens the menu, **F7** toggles freecam (both rebindable under Controls > Misc).
Settings save to `config/katana-client.json`.

## Build without installing anything (GitHub)
1. Create a GitHub repo and upload the *contents* of this folder (include the hidden `.github` folder).
2. Open the **Actions** tab, let "build" run (or click Run workflow).
3. Download the `katana-client` artifact; the jar inside is the mod.
4. Put it in `.minecraft/mods` with Fabric Loader 0.18+ and Fabric API for 1.21.11.

Local build: JDK 21, `gradle wrapper --gradle-version 9.2.1`, then `./gradlew build`.

## Freecam notes
- Your real player stays where it is and can still be attacked or take damage. Do not use it in danger.
- Attack/use/drop/swap keys are blocked while freecam is on, so you cannot hit or place by accident.
- Freecam is visual only, but some servers ban it. Check the rules of any server you join.

## Verification status
Not compiled against real Minecraft (no network where this was written). Pure logic (hex/HSV, click counter) was tested.
If the first build errors, the likeliest spots are:
- `HudRenderCallback` (deprecated; if removed, switch to `HudElementRegistry`)
- `g.pose().pushMatrix()/translate/scale` (1.21.6+ 2D matrix stack)
- `KeyMapping.Category.MISC`, `Minecraft.getFps()`
- Mixins: `Camera` target method names (`setup`/`update`), `setPosition`, `setRotation`, `detached`, and `Entity.turn`
- `new ClientInput() { }` in Freecam (the input swap that freezes the real player)
