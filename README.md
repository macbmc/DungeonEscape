# Dungeon Escape - 2D Top-Down Android Game

A procedural rogue-lite dungeon crawler built for Android using pure **Kotlin**, **Jetpack Compose**, **Compose Canvas**, **Coroutines**, and **StateFlow**.

---

## 🎮 Game Features & Mechanics

- **Procedural 25x25 Dungeons**: Every dungeon level is procedurally generated with connected rooms and corridors, with 100% guaranteed reachability verified via BFS flood-fill.
- **Dynamic Lighting & Fog of War**: The player acts as a dynamic torchlight source with a 6-tile vision radius and smooth radial falloff. Unexplored regions remain hidden in darkness while explored areas maintain ambient memory.
- **Responsive Mobile Controls**: Multi-touch virtual joystick on the bottom-left with deadzone handling, plus dedicated Attack and Dash buttons on the bottom-right.
- **Smooth 60 FPS Canvas Game Loop**: Coroutine-based frame ticker using `Canvas` rendering without recomposition overhead.
- **Combat & Dash System**:
  - **Melee Slash**: 160-degree frontal attack arc with visual sweep.
  - **Dash Ability**: Burst speed passing through enemies with trailing cyan particles and a 3-second cooldown ring.
  - **Enemy AI (Skeletons)**: Wanders randomly and detects player within range, featuring intelligent soft separation forces.
  - **Invulnerability Window & Damage Flash**: 1-second invulnerability on hit with blinking frames and crimson hit particles.
- **Level Progression & Difficulty Scaling**:
  - Collect coins (+10 score) and the golden key to activate the Exit Portal.
  - Each new level scales difficulty: `+1 enemy`, `+10% enemy speed`, and `+5% enemy health`.
- **Integrated Audio System**: Multi-channel `SoundPool` with crash-proof procedural sound synthesis fallback for all game events (Attacks, Hits, Pickups, Portal, Victory, Game Over, and BGM).

---

## 🏗️ Architecture & Project Structure

```
com.dungeonescape/
|-- MainActivity.kt                 // Lifecycle management, Compose UI root
|-- ui/
|   |-- MainMenuScreen.kt           // Menu, instructions dialog, and stats
|   |-- GameScreen.kt               // 60FPS Compose Canvas layer & controls
|   |-- GameOverScreen.kt           // Score breakdown and replay options
|   |-- components/
|   |   |-- VirtualJoystick.kt      // Multi-touch drag thumbstick
|   |   |-- ActionButton.kt         // Action button with cooldown indicator
|   |   |-- GameHud.kt              // HP bar, Key status, coins, level, pause
|   |-- theme/                      // Theme, Color palette, and Typography
|-- engine/
|   |-- GameEngine.kt               // Main game coordinator & deterministic tick
|   |-- GameLoop.kt                 // 60 FPS Coroutine frame ticker
|   |-- GameStateManager.kt         // High score and persistent progress
|   |-- CollisionSystem.kt          // Wall sliding, enemy separation, attacks
|   |-- CameraSystem.kt             // Smooth lerp follow & viewport culling
|   |-- LightingSystem.kt           // Torchlight raycasting & fog of war
|   |-- DungeonGenerator.kt         // Procedural 25x25 dungeon with BFS solver
|   |-- ParticleSystem.kt           // Thread-safe particle pool
|-- entities/
|   |-- Player.kt                   // Player stats, dash & attack state machine
|   |-- Skeleton.kt                 // Skeleton enemy AI & stats
|   |-- Coin.kt                     // Animated coin pickup
|   |-- KeyItem.kt                  // Key pickup with glow effect
|   |-- ExitPortal.kt               // Portal vortex animation
|   |-- Particle.kt                 // Particle physics & alpha decay
|-- models/
|   |-- Position.kt, Vector2D.kt    // Coordinate and vector math
|   |-- Tile.kt, TileType.kt        // Grid tiles
|   |-- GameState.kt, LevelState.kt // Immutable snapshots exposed to UI
|-- audio/
|   |-- AudioManager.kt             // SoundPool + safe procedural synthesis
|   |-- SoundType.kt                // Audio events enum
|-- utils/
|   |-- Constants.kt                // Balanced gameplay and engine constants
|   |-- Extensions.kt               // Math & coordinate helper extensions
|   |-- RenderUtils.kt              // Compose Canvas drawing routines
```

---

## 🛠️ Build & Run Instructions

1. **Prerequisites**: Android Studio Meerkat or newer / Android SDK 34+.
2. **Build debug APK**:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
3. **Run Unit Tests**:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
4. Output APK location:
   `app/build/outputs/apk/debug/app-debug.apk`
