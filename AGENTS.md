# AGENTS.md

Skadi: survivor-centered Android anti-tracking tool hidden inside a working cover app (Kotlin, Jetpack Compose, single module `:app`).

## Commands
- No `gradlew` scripts or wrapper JAR are committed. Generate once: `gradle wrapper --gradle-version 8.9` (or open in Android Studio, JDK 17).
- Build: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk` (use a real phone, not an emulator)
- Lint: `./gradlew lint` (standard AGP task)
- Tests: none exist yet.

## Where logic lives (`app/src/main/java/com/smiraj/meditation/`)
- `AppViewModel.kt` — `Screen` state; `onCustomDurationEntered()` is the ONLY entry to the hidden layer
- `MainActivity.kt` — screen switching, `FLAG_SECURE` on hidden screens, reset to cover on `ON_STOP` and back
- `diagnostics/`, `safety/` — hidden screens (diagnostics, safety gate, Leči/Seči)
- `scan/ScanSnapshot.kt` — in-memory findings model; the real scanner is planned in `docs/REAL_SCANNER_EXECUTION_PLAN.md`
- `data/` — Room + DataStore for cover-app data only

## Safety rules (do not regress)
- Never persist scan results, evidence, or any hidden-mode flag (no Room, DataStore, files, logs).
- No visible "secret" UI element; keep the single trigger funnel.
- Never read passwords; no accessibility services or overlays to observe other apps (that is stalkerware behavior).
- No automatic deletion or batch actions; every action goes through the safety gate and safe order.
- Hidden screens use neutral wording; no runtime mock findings in the clean shell.
- Do not add screenshots of the app, or describe the cover app's appearance, in public docs.

## Conventions
- Kotlin 2.0 Compose compiler plugin (no `composeOptions` / `kotlinCompilerExtensionVersion`); Room uses `ksp`, not `kapt`.
- Versions live in `gradle/libs.versions.toml`.
- Project notes (`SKADI.md`, `PROGRESS.md`) are in Serbian; UI strings are in `res/values/strings.xml`.

## Do not commit
- IDE files: `.idea/` (currently committed by mistake; don't add more), `*.iml`, `local.properties`
- Build output: `build/`, `app/build/`, `.gradle/`, APKs
- Signing keys / keystores

Humans: see README.md
