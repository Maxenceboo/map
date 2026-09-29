---
description: "Three.js 3D Vehicle Engine and MapLibre GL WebGL Custom Layer Rules"
globs: ["src/services/vehicle3D*.ts", "src/components/Vehicle3DPreview.tsx"]
always_on: true
---

# 3D Vehicle Engine Rules

- **Native WebGL Custom Layer**: `Vehicle3DLayer` renders Three.js meshes directly inside MapLibre GL's WebGL context via `mercatorMatrix`.
- **Coordinate System**:
  - `+Z`: Vehicle forward direction (front bumper, headlights, motion).
  - `-Z`: Vehicle rear direction (exhaust, taillights, spoiler).
  - `+Y`: Sky / Up.
  - `+X`: Vehicle right side.
- **Headlight Beams**:
  - Must originate at `z = frontZ` and point towards `+Z`.
  - Use `createBeamVolumeTexture()` (linear alpha gradient fading to 0.0) to eliminate any visible circular cutoff cap.
  - Combine with `createRoadLightTexture()` on a flat asphalt ground plane (`y = 0.025`).
  - Use `THREE.NormalBlending` for WebGL compatibility with Android WebView alpha compositing.
- **Scale Stability**: The custom layer computes an adaptive scale based on `pixelsPerMeter` and `targetPixelSize (~58px)` so that vehicles maintain consistent visual clarity across zoom levels.
