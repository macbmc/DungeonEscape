# Dungeon Escape — Complete Technical Documentation

## 1. Executive Summary & Architecture Overview

**Dungeon Escape** is a high-performance, top-down rogue-lite procedural dungeon crawler engineered natively for Android using pure **Kotlin**, **Jetpack Compose**, **Compose Canvas**, **Kotlin Coroutines**, and **StateFlow**.

The engine adheres to a **lightweight data-driven game architecture** designed to run at a rock-solid **60 FPS** on mid-range Android devices, completely avoiding unnecessary garbage collections, heavyweight DI frameworks, and Compose recomposition bottlenecks.

```
+--------------------------------------------------------------------------------------------------+
|                                    JETPACK COMPOSE UI LAYER                                      |
|                                                                                                  |
|   +--------------------------+  +--------------------------------+  +-------------------------+  |
|   |     MainMenuScreen       |  |          GameScreen            |  |      GameOverScreen     |  |
|   |  - Title & Ambient Grid  |  |  - Fullscreen Canvas Viewport  |  |  - Score & Stats        |  |
|   |  - Resume (Saved Level)  |  |  - HUD (HP, Key, Theme, Coins) |  |  - High Score Badge     |  |
|   |  - Theme Mode Selector   |  |  - Virtual Joystick (Multi-T)  |  |  - Replay / Menu        |  |
|   |  - Instructions Dialog   |  |  - Attack / Dash Buttons       |  |                         |  |
|   +--------------------------+  |  - Merchant Altar Dialog (Shop)|  |                         |  |
|                                 +--------------------------------+  +-------------------------+  |
+--------------------------------------------------------------------------------------------------+
                                                 ▲
                                                 │ Immutable GameState (StateFlow)
                                                 │ Atomic UI Action Events
+--------------------------------------------------------------------------------------------------+
|                                        GAME ENGINE LAYER                                         |
|                                                                                                  |
|   +--------------------------+  +--------------------------------+  +-------------------------+  |
|   |         GameLoop         |->|           GameEngine           |->|     GameStateManager    |  |
|   |  - Coroutine 60 FPS      |  |  - Deterministic Tick Executor |  |  - Persistent Progress  |  |
|   |  - Dynamic Timestep      |  |  - Atomic Input Queue          |  |  - SharedPreferences    |  |
|   |  - Nano Precision Clamped|  |  - State Publishing            |  |  - Session Save/Resume  |  |
|   +--------------------------+  +--------------------------------+  +-------------------------+  |
|                                                │                                                 |
|          +--------------------+----------------+--------------------+---------------------+      |
|          │                    │                                     │                     │      |
|          ▼                    ▼                                     ▼                     ▼      |
|   +---------------+   +----------------+                     +----------------+    +------------+|
|   | DungeonGen    |   | CollisionSys   |                     | LightingSystem |    | ThemeMgr   ||
|   | - Rooms/Paths |   | - Wall Sliding |                     | - 6-Tile Torch |    | - 5 Themes ||
|   | - Secret Rooms|   | - Enemy Repulse|                     | - Fog Raycast  |    | - Palette  ||
|   | - Solvability |   | - Secret Wall  |                     | - Smoothstep   |    | - Cache    ||
|   +---------------+   +----------------+                     +----------------+    +------------+|
|          │                    │                                     │                     │      |
|          ▼                    ▼                                     ▼                     ▼      |
|   +---------------+   +----------------+                     +----------------+    +------------+|
|   | ParticleSys   |   | AudioManager   |                     | CameraSystem   |    | Entities   ||
|   | - Safe Pool   |   | - Worker Queue |                     | - Lerp Follow  |    | - Player   ||
|   | - Zero Alloc  |   | - Synth PCM    |                     | - Viewport Cull|    | - Skeleton ||
|   | - 120 Max Capped  | - SoundPool FX |                     | - Sub-pixel    |    | - Chests   ||
|   +---------------+   +----------------+                     +----------------+    +------------+|
+--------------------------------------------------------------------------------------------------+
```

---

## 2. Complete Flow & Lifecycle

### 2.1 Application Launch Flow
1. **Activity Initialization**: `MainActivity.onCreate()` initializes `GameEngine` and `GameLoop`.
2. **State Hydration**: `GameStateManager` checks `SharedPreferences` for any previous session (`hasSavedGame`).
3. **Main Menu Display**:
   - Displays Highest Level and High Score.
   - If a saved game session exists, a green **`RESUME (LVL X)`** button is presented.
   - Clicking **`NEW GAME`** opens the Theme Selection Dialog where the player can choose **Progressive Run** or lock a specific **Theme** (Ruins, Crypt, Ice, Lava, Shadow).
4. **Game Loop Start**: Calling `gameLoop.start()` spawns a dedicated coroutine on `Dispatchers.Default` executing at 60 FPS.

### 2.2 In-Game Execution Flow (Per Frame)
Every frame (targeted at 16.6ms / 60 FPS), `GameLoop` measures the elapsed nanoseconds (`deltaTime`), clamps it to `0.05s`, and delegates to `GameEngine.update(deltaTime)`:

```
[Frame Start: System.nanoTime()]
       │
       ▼
[1. Process Queued Inputs] ───► Atomic check for Joystick Vector, Attack, Dash
       │
       ▼
[2. Player Movement & Physics] ──► Resolve Dash Burst or Joystick Velocity against Walls
       │
       ▼
[3. Enemy AI & Separation] ───► State Machine (Wander/Chase) + Soft Repulsion + Wall Slides
       │
       ▼
[4. Interaction & Collisions] ─► Secret Wall hits, Chest openings, Traps, Pickups, Enemy Hits
       │
       ▼
[5. Particle System Update] ──► Age decay, friction velocity, pool recycling
       │
       ▼
[6. Camera Tracking] ─────────► Exponential lerp interpolation toward player
       │
       ▼
[7. Fog of War & Lighting] ───► 6-Tile Line-of-Sight Raycasting & Smoothstep Radial Falloff
       │
       ▼
[8. Publish Immutable State] ─► Emit snapshot to StateFlow<GameState> -> Compose Canvas redraws
       │
       ▼
[Frame End: Yield/Delay to 60 FPS target]
```

### 2.3 Session Persistence Flow
- When the player clears a level, pauses the game, or exits to the main menu without dying, `gameEngine.saveSession()` saves the active level, score, player health, coins, theme mode, and locked theme.
- When the player dies, `stateManager.recordScore(...)` records the high score and clears the saved session.

---

## 3. Core Engine Components & Subsystems

### 3.1 Procedural Dungeon Generator (`DungeonGenerator.kt`)
- **Grid Dimension**: 25x25 tiles.
- **Room Placement**: Generates 4 to 7 non-overlapping rectangular rooms (4x4 to 6x6) with boundary constraints.
- **Corridor Carving**: Connects rooms via L-shaped orthogonal hallways with horizontal/vertical bifurcation.
- **100% Solvability Guarantee**:
  - Executes a Breadth-First-Search (BFS) flood fill from the player spawn in Room 0.
  - Places the Golden Key and Exit Portal only on verified reachable floors.
  - The main quest path is guaranteed solvable without breaking secret walls.
- **Secret Treasure Room Generation**:
  - Based on level progression (35% at L1-3, 65% at L4-10, 90% at L11+).
  - Finds an isolated wall adjacent to an existing room and carves a secret chamber.
  - Places a `SECRET_WALL` (`hitsRemaining = 3`) at the entryway.
  - Populates the room with:
    - 1 `TreasureChest` (+40 coins)
    - 1 `MerchantAltar` (Health potions)
    - Extra coin clusters
    - Risk vs Reward: Guarded by an `ANCIENT_GUARDIAN` or `TRAP_SPIKE` hazards.

### 3.2 Progressive Theme System (`ThemeManager.kt`)
The theme system modifies the visual and acoustic ambiance based on level or user selection:

| Theme | Levels | Floor Stones | Wall & Shadows | Torch Tint | Ambient Audio |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Ancient Ruins** | 1–5 | Earthy Slate | Mossy Granite | Warm Orange | A-minor Ruin Drone |
| **Haunted Crypt** | 6–10 | Dark Crypt | Eerie Slate | Spectral Green | Low Crypt Dirge |
| **Frozen Caverns** | 11–15 | Glacier Blue | Frost Ice | Frost Cyan | High Crisp Winds |
| **Lava Fortress** | 16–20 | Basalt Magma | Crimson Basalt | Magma Orange | Volcanic Bass |
| **Shadow Realm** | 21+ | Void Obsidian| Void Purple | Deep Violet | Dark Void Resonance|

All theme color palettes and brush assets are pre-instantiated in `ThemeManager` to guarantee **zero runtime object allocation**.

### 3.3 Dynamic Fog of War & Lighting (`LightingSystem.kt`)
- **Vision Radius**: 6 tiles.
- **Line-of-Sight Raycasting**: Uses Bresenham-style ray casting in a bounding box to verify unobstructed line of sight. Walls block sight into hidden corridors, but the wall face itself is illuminated.
- **Smoothstep Radial Falloff**:
  $$\text{falloff} = \left(1 - \frac{\text{dist}}{\text{visionRadius}}\right)$$
  $$\text{smoothLevel} = \text{falloff}^2 \cdot (3 - 2 \cdot \text{falloff})$$
  $$\text{lightLevel} = 0.25 + 0.75 \cdot \text{smoothLevel}$$
- **Ambient Memory**: Tiles previously revealed retain `isExplored = true` with a dim ambient illumination of `0.18f`, while unvisited areas remain pitch black (`0.0f`).

### 3.4 Collision & Physics System (`CollisionSystem.kt`)
- **AABB-Circle Wall Sliding**: Moving entities (Player, Skeletons) check bounding tile intersections along X and Y axes independently. If moving diagonally into a wall, the blocked axis is zeroed while the free axis continues moving, resulting in silky smooth wall sliding.
- **Soft Enemy Repulsion**: Iterates all active skeleton pairs. If distance $< 2 \cdot r_{\text{col}}$, applies an outward separation impulse $\vec{F}_{\text{repulse}} = \frac{\Delta \vec{p}}{\|\Delta \vec{p}\|} \cdot \frac{2r - d}{2}$.
- **Secret Wall Attacks**: Melee slashes calculate distance and angular difference to adjacent `SECRET_WALL` tiles. 3 successful hits break the wall, instantly converting it to a walkable `FLOOR` tile.
- **Trap Detection**: Detects player foot collisions with extending floor spikes and inflicts 12 damage unless dashing or invulnerable.

### 3.5 Combat, Dash & Entities (`Player.kt`, `Skeleton.kt`)
- **Player Stats**: 100 HP, 4.0 tiles/sec speed.
- **Melee Slash**: 1.35-tile radius with a $160^\circ$ frontal cone angle.
- **Dash Mechanic**: 3.5x burst speed, 0.18s duration, 3.0s cooldown. Passes through enemies without taking damage.
- **Invulnerability**: 1.0-second invulnerability window after taking damage with sprite blinking.
- **Enemy Variants**:
  - `NORMAL`: Standard skeleton.
  - `ELITE`: $+50\%$ HP, $+25\%$ Speed, radiant golden aura.
  - `ANCIENT_GUARDIAN`: $+100\%$ HP, $+15\%$ Speed, guards treasure rooms, drops $+25$ bonus coins.
  - `SHADOW_ELITE`: Level 11+ assassin with distance-based invisibility ($> 2.5$ tiles away $\to$ translucent alpha).

### 3.6 Merchant Altar & Potion Economy (`MerchantDialog.kt`)
- Located inside Secret Treasure Rooms.
- Instant purchase upon interaction:
  - **Small Potion**: 20 Coins $\to$ $+25$ HP.
  - **Medium Potion**: 50 Coins $\to$ $+60$ HP.
  - **Large Elixir**: 100 Coins $\to$ Full HP ($100$ HP).
- No inventory baggage or menu delay: instant deduction and healing.

### 3.7 Crash-Proof Audio Engine (`AudioManager.kt`)
- **Dual Architecture**:
  - Low-latency `SoundPool` for instantaneous SFX.
  - Background procedural sound synthesizer using 16-bit PCM `AudioTrack` fallback.
- **Concurrency & Throttling Protection**:
  - Dedicated sound queue worker channel (`Channel<SoundType>`).
  - Time-based debouncing (min 80–120ms between identical sound bursts).
  - All native calls guarded by `state == STATE_INITIALIZED` and wrapped in `try-catch(Throwable)`.

---

## 4. Performance & Optimization Highlights

1. **Zero Garbage Collection Overhead**:
   - Vector operations and math utilities reuse primitive variables.
   - Pre-allocated particle pool capped at 120 elements with in-place recycling.
   - Cached theme brushes and paints in `RenderUtils`.
2. **Recomposition Isolation**:
   - The game world is rendered entirely inside a single `Compose Canvas` lambda reading from `GameState`.
   - Touch controls and HUD elements use isolated layout containers to prevent Canvas redraw invalidations.
3. **Viewport Culling**:
   - `CameraSystem.getVisibleTileBounds(...)` calculates visible tile limits based on screen resolution and only sends visible entities to the Canvas.

---

## 5. Build & Test Verification

- **Unit Tests**:
  - `DungeonGeneratorTest.kt`: Solvability BFS checks across 20 procedural dungeons.
  - `CollisionSystemTest.kt`: Wall sliding, 3-hit secret wall breakage, traps, chests, enemy separation.
  - `ThemeManagerTest.kt`: Progressive level mapping and asset preloading.
  - `GameStateManagerTest.kt`: Save/Resume data integrity.
- **Build Execution**:
  - `./gradlew.bat testDebugUnitTest` $\to$ **100% Passed**.
  - `./gradlew.bat assembleDebug` $\to$ **BUILD SUCCESSFUL**.
