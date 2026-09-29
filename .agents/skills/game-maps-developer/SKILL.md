---
name: game-maps-developer
description: Comprehensive development skill for Game Maps IRL. Use when modifying or debugging 3D Three.js vehicle models, MapLibre custom WebGL layers, UI HUD elements, sound synthesis, or executing the Android Capacitor build & ADB deployment cycle.
---

# Game Maps IRL Developer Skill

This skill provides step-by-step guidance and runbooks for developing, testing, and deploying the **Game Maps IRL** project.

## Project Overview

- **Concept**: Video game style real-world GPS navigation (GTA V, Cyberpunk 2077, Need for Speed).
- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS 4.
- **Map & Routing**: MapLibre GL v6, OSRM (Open Source Routing Machine).
- **Vehicle Rendering**: Native WebGL custom layer in MapLibre running Three.js synchronously (`Vehicle3DLayer`).
- **Mobile**: Android Capacitor with native Android Auto / Car App Service integration.

---

## Directives & Coding Standards

### 1. Minimalist & Flat UI
- **Never use generic rounded bubble cards** (`rounded-3xl`, large drop shadows).
- Use flat dark cockpit HUD surfaces: `bg-neutral-900`, `bg-black`, `border-neutral-800`, `rounded-xl`.
- Group settings into vertical sub-menus with back buttons (`< Précédent`), not grids of cards.
- Keep the main search bar wide, readable, and free of clutter.

### 2. 3D Vehicle Engine Architecture
- **Layer**: `src/services/vehicle3DLayer.ts` implements MapLibre's `CustomLayerInterface` (`renderingMode: '3d'`).
- **Coordinate Conventions**:
  - `+Z`: Vehicle forward direction (front bumper, headlights, travel).
  - `-Z`: Vehicle rear direction (taillights, spoiler, diffuser).
  - `+Y`: Vehicle top / up.
  - `+X`: Vehicle right side.
- **Headlights**:
  - Forward-projecting volumetric cone starting at `z = frontZ` pointing along `+Z`.
  - Must use `createBeamVolumeTexture()` (linear gradient fading to `alpha = 0`) to eliminate circular cutoff caps.
  - Combine with road light pool plane (`createRoadLightTexture`) at `y = 0.025` on asphalt.
  - Always use `THREE.NormalBlending` for Android WebView compositor compatibility.

---

## Android Build & Deployment Runbook

When developing or verifying changes on a connected Android device:

```bash
# 1. Build Web Assets
npm run build

# 2. Sync to Capacitor
npx cap sync android

# 3. Assemble Debug APK
cd android
./gradlew assembleDebug
cd ..

# 4. Install onto Device via ADB
adb install -r android/app/build/outputs/apk/debug/app-debug.apk

# 5. Launch the Application
adb shell monkey -p com.gamemaps.irl -c android.intent.category.LAUNCHER 1

# 6. Capture Verification Screenshot
adb shell screencap -p /sdcard/s.png
adb pull /sdcard/s.png screen_verification.png
```

---

## Audio Engine Guidelines

- Uses the Web Audio API procedurally in `src/services/soundEffects.ts`.
- GTA V "Mission Passed" jingle on destination arrival.
- Subtle UI feedback on clicks and route recalculations.
- Audio toggle in `Paramètres > Audio` with state persisted in `localStorage`.
