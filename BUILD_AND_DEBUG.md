# Smiraj (Skadi decoy) — Build & Debug

Phase 1 decoy: a fully working meditation app. **No secret layer is present yet** —
that's deliberate (per `SKADI_DESIGN_MEDITATION.md` §0). The one hook for Phase 2
is `AppViewModel.onCustomDurationEntered()` — every custom-minute entry funnels
through it, so the trigger drops in there later without touching the cover UI.

## What's here
- `com.smiraj.meditation` — single-Activity Jetpack Compose app, minSdk 26 / target 35.
- Cover screen: breathing-circle animation, presets (3/5/10/15) + custom field, Počni/Zaustavi.
- Tabs: Meditiraj / Istorija (streak + totals) / Podešavanja (ambient + keep-screen-on).
- Room DB (`smiraj.db`) stores **only** sessions. DataStore stores settings. Nothing else persists.
- Ambient sound is **wired but silent** — add loop files to `app/src/main/res/raw/`
  (`rain.ogg`, `forest.ogg`) and fill in `AmbientPlayer.rawResFor()`.

## First open in Android Studio
1. **File → Open** the `Skadi/` folder (Giraffe/Koala or newer; uses JDK 17, bundled with AS).
2. The Gradle **wrapper JAR isn't committed** (binary). On first sync AS regenerates it,
   or run once in a terminal that has Gradle: `gradle wrapper --gradle-version 8.9`.
3. If AS shows a stale non-Gradle module from the old `.idea/`, do
   **File → Invalidate Caches / Restart**, or delete `.idea/` and reopen — it re-imports as Gradle.
4. Let it sync. AGP 8.5.2 / Kotlin 2.0.21 / Compose BOM 2024.10.01 / Room 2.6.1.

## Run & debug on a real phone (recommended over emulator)
The design notes call for a real device — the secret layer (later) reads installed
apps and permissions, which an emulator can't show realistically.

```bash
# enable Developer options + USB debugging on the phone, then:
adb devices                 # confirm it's listed
adb install -r app/build/outputs/apk/debug/app-debug.apk   # or just hit Run in AS
adb logcat --pid=$(adb shell pidof -s com.smiraj.meditation)  # live logs for our app only
```

Useful filters while debugging:
```bash
adb logcat *:E                      # errors only
adb logcat | grep -i smiraj         # our tags / package
adb shell run-as com.smiraj.meditation ls databases/   # confirm smiraj.db exists
```

## Quick functional checklist (decoy must survive 10s of someone else scrolling)
- [ ] App launches to Meditiraj, breathing circle resting.
- [ ] Pick a preset → Počni → circle animates, label toggles Udahni/Izdahni, timer counts down.
- [ ] Zaustavi mid-session, then let one run to 0:00 → both land in Istorija.
- [ ] Istorija shows streak / session count / total minutes; survives app restart (Room).
- [ ] Custom field: type e.g. `7` → starts a 7-min session (this is the Phase-2 trigger path).
- [ ] Podešavanja: ambient radio + keep-screen-on switch persist across restart (DataStore).

## Likely build snags & fixes
- **"Unresolved reference: collectAsStateWithLifecycle"** → make sure
  `androidx-lifecycle-runtime-compose` is synced (it's in `libs.versions.toml` + `app/build.gradle.kts`).
- **"Compose compiler / Kotlin version mismatch"** → we use the Kotlin 2.0 Compose plugin
  (`org.jetbrains.kotlin.plugin.compose`), not the old `composeOptions` block. Don't re-add `kotlinCompilerExtensionVersion`.
- **Room "Cannot find setter / schema" / KSP errors** → confirm the `ksp` plugin is applied and
  `room-compiler` is on the `ksp(...)` config (not `kapt`).
- **JDK error on sync** → File → Settings → Build Tools → Gradle → Gradle JDK = 17.
- **Icons unresolved (`Icons.Filled.Air`)** → `material-icons-extended` dependency must be synced.

## Important guardrails (don't regress these in Phase 2)
- Persist **only** meditation sessions. Never write scan results, evidence, or a
  "secret mode" flag to disk.
- Keep the custom-timer field as the single trigger funnel — no visible "secret" UI element.
- Build the secret layer behind `onCustomDurationEntered()`, `FLAG_SECURE`, and the
  `onStop()` panic-reset, exactly as the design spec stages it.
