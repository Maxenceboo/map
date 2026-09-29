# AGENTS.md — Agent & Pair Programmer Guidelines

Welcome to the **Game Maps IRL** repository. When working on this codebase, always adhere to the project conventions and user design preferences outlined below.

## Core Rules

1. **Flat & Minimalist UI (No generic AI rounded cards)**:
   - Avoid `rounded-2xl` / `rounded-3xl` cards with floating shadows.
   - Use flat, dark HUD surfaces (`bg-neutral-900`, `bg-black`, `border-neutral-800`, `rounded-xl`).
   - Keep settings organized in vertical hierarchical sub-menus with back buttons.

2. **3D Three.js Vehicle Layer**:
   - The vehicle rendering engine is a native WebGL MapLibre custom layer (`src/services/vehicle3DLayer.ts`) running Three.js synchronously with MapLibre's render loop.
   - Forward orientation is `+Z` (direction of travel).
   - Headlight beams must emerge from the front bumper (`z = frontZ`), project forward on the road, with a soft fading linear gradient and a ground asphalt light pool plane.

3. **Status Indicators**:
   - Keep search bar clear and wide. Status indicators (GPS lock, speed, audio) are placed discreetly on the top-right and bottom-left.

4. **Android Build & Testing**:
   - Always verify on device with:
     `npm run build && npx cap sync android && cd android && ./gradlew assembleDebug && cd .. && adb install -r android/app/build/outputs/apk/debug/app-debug.apk && adb shell monkey -p com.gamemaps.irl -c android.intent.category.LAUNCHER 1`

Refer to [GEMINI.md](file:///c:/Users/maxen/Documents/antigravity/map/GEMINI.md) and `.context/SESSION_HISTORY.md` for complete technical details.
