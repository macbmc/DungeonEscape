# Dungeon Escape — Asset & Open Source Licenses

This document outlines the licensing, attribution, and status of all third-party libraries and assets utilized in **Dungeon Escape**.

---

## 1. Open Source Libraries & Frameworks

| Library / Component | Source | License | Attribution / Notice |
| :--- | :--- | :--- | :--- |
| **Android Jetpack Compose** | `androidx.compose.*` | Apache License 2.0 | Copyright The Android Open Source Project |
| **AndroidX Core & Splashscreen** | `androidx.core:core-ktx`, `androidx.core:core-splashscreen` | Apache License 2.0 | Copyright The Android Open Source Project |
| **AndroidX Activity Compose** | `androidx.activity:activity-compose` | Apache License 2.0 | Copyright The Android Open Source Project |
| **AndroidX Lifecycle** | `androidx.lifecycle:lifecycle-runtime-ktx` | Apache License 2.0 | Copyright The Android Open Source Project |
| **Material 3 Design** | `androidx.compose.material3:material3` | Apache License 2.0 | Copyright The Android Open Source Project |
| **Kotlin & Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-*` | Apache License 2.0 | Copyright JetBrains s.r.o. |
| **JUnit 4** (Test dependency) | `junit:junit` | Eclipse Public License 1.0 | Copyright JUnit |

---

## 2. Audio Engine & Sound Effects

| Asset / Sound Component | Implementation | License / Ownership | Status |
| :--- | :--- | :--- | :--- |
| **Sound Synthesis Engine** | Procedural 16-bit PCM AudioTrack Synthesizer (`AudioManager.kt`) | Proprietary / Custom Project Code | Safe for commercial distribution |
| **Attack Sweep** | Dynamic Frequency Synthesized Sweep (440Hz → 120Hz) | Custom Project Code | Safe |
| **Enemy Hit SFX** | Dynamic Frequency Synthesized Sweep (220Hz → 60Hz) | Custom Project Code | Safe |
| **Player Damage SFX** | Dynamic Low Frequency Pulse (180Hz → 80Hz) | Custom Project Code | Safe |
| **Coin Pickup SFX** | Dual-Tone Arpeggio (987Hz / 1318Hz) | Custom Project Code | Safe |
| **Key Pickup SFX** | Quad-Tone Major Chime (523Hz / 659Hz / 783Hz / 1046Hz) | Custom Project Code | Safe |
| **Portal Activation SFX** | Ascending Harmonic Sweep (200Hz → 880Hz) | Custom Project Code | Safe |
| **Dash Whoosh SFX** | High-Velocity Air Sweep (600Hz → 250Hz) | Custom Project Code | Safe |
| **Victory & Clear SFX** | Ascending Victory Chime (523Hz → 1046Hz) | Custom Project Code | Safe |
| **Ambient BGM Themes** | Real-time procedural harmonic note generator | Custom Project Code | Safe |

---

## 3. Visual Assets & Artwork

| Asset | Path | Type | License / Status |
| :--- | :--- | :--- | :--- |
| **Launcher Icon Background** | `res/drawable/ic_launcher_background.xml` | Vector Drawable | Custom Project Asset — Safe |
| **Launcher Icon Foreground** | `res/drawable/ic_launcher_foreground.xml` | Vector Drawable | Custom Project Asset — Safe |
| **Canvas Game Sprites** | `RenderUtils.kt` (Hero, Skeletons, Tiles, Chests, Altars, Keys, Portals) | Native Compose Canvas Code | Custom Project Code — Safe |
| **Main Menu Background** | `res/drawable/bg_main_menu.jpg` | Bitmap Artwork | **LICENSE VERIFICATION REQUIRED BEFORE RELEASE** (Verify source license before commercial store submission) |

---

## 4. Apache License 2.0 Summary

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
