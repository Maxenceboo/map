---
description: "Android Capacitor & ADB Build and Deployment Workflow"
globs: ["android/**/*", "capacitor.config.*"]
always_on: false
---

# Android Capacitor & ADB Workflow

1. Always run `npm run build && npx cap sync android` when updating front-end code.
2. Build debug APK: `cd android && ./gradlew assembleDebug && cd ..`.
3. Install via ADB: `adb install -r android/app/build/outputs/apk/debug/app-debug.apk`.
4. Launch app: `adb shell monkey -p com.gamemaps.irl -c android.intent.category.LAUNCHER 1`.
5. Capture screenshot for verification: `adb shell screencap -p /sdcard/s.png && adb pull /sdcard/s.png screen_test.png`.
