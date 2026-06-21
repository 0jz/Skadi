# Sunčica — Session Progress Log

Branch: `integration/clean-secret-shell`
Period: this working session (3 work rounds)
Scope: redesign Skadi's disguise into a weather app ("Sunčica"), build the hidden
safety app in a matching theme, and make the Mir/Uči sections functional.

---

## 1. Starting point — repo review

- Local `integration/clean-secret-shell` was in sync with `origin` (0 ahead / 0 behind),
  but the working tree had 58 modified files.
- 55 of those were **CRLF line-ending churn only** (no real content change).
- 3 files were **truncated/corrupted** in the working tree (cut off mid-line, missing
  their closing code): `scan/AccountAudit.kt`, `scan/PackageScanner.kt`,
  `scan/SpecialAccessChecker.kt`. As-is, the project would not compile.
- The branch was 9 commits ahead of `main`, mostly AppOps/permission-detection fixes.

**Action:** restored the 3 truncated files from their committed versions. Working tree
is now clean of real changes apart from the new feature work below.

---

## 2. Decoy changed: meditation app → weather app "Sunčica"

The public "cover" used to be a meditation/water-tracker app. It is now a weather app
that looks like Apple's iOS Weather app.

- New `weather/WeatherScreen.kt` — Layer 1 public face: deep-blue gradient
  (#1C3A5E → #081830), Belgrade 19°, "Delimično oblačno", hourly + weekly
  forecast in frosted-glass cards, **no bottom navigation**. Static mock data
  (works offline, no API key).
- **Secret entry:** double-tap the temperature number → opens the hidden app.
  No visible PIN field, nothing suspicious on screen.
- App display name renamed to **Sunčica** (`strings.xml`; package id unchanged so
  the build doesn't break).
- Old meditation cover (3-tab Scaffold) removed from `MainActivity`.

---

## 3. Hidden app rebuilt as a themed 5-tab interface

The secret menu now shares **one identical visual language** with the decoy — there is
no visual "switch" when entering the real app.

- New `weather/SuncicaUi.kt` — shared design system: the `Suncica` color tokens
  (gradient, frosted card, status colors), plus reusable composables
  (`SuncicaBackground`, `FrostedCard`, `CardDivider`, `ScreenHeader`, `ListRow`,
  `SuncicaNavBar`). The weather decoy was refactored to use these too.
- New `suncica/SafeApp.kt` — Layer 2 hidden app with a bottom nav of 5 tabs, matching
  the design handoff (HTM): **SOS, Mapa, Sken, Mir, Uči**.
  - **SOS** — concentric "hold" rings + shield, "Sigurna si" pill, contacts list (112 dials).
  - **Mapa** — search bar, color-coded safety zones (green/orange/red) + legend.
  - **Sken** — wired to the **real existing device scanner**; runs a live scan and shows
    findings (severity-colored) in themed cards.
  - **Mir** — calm & support (see section 5).
  - **Uči** — education topics (see section 5).
- Routing: `AppViewModel.enterDiagnostics()` now opens `Screen.SafeApp`; system back is a
  panic exit straight to the weather cover. Added `AppViewModel.startScan()` to run the
  scan in place from the Sken tab.

---

## 4. App icon redesigned

- `ic_launcher_foreground.xml` — a little sun (yellow disc + 8 rays), replacing the old
  breathing-circle mark.
- `ic_launcher_background.xml` — deep-blue Apple-Weather gradient; `colors.xml` updated
  to `#1C3A5E`.

---

## 5. Mir & Uči made functional

Previously these tabs were static mockups. They now work:

- **Mir → Disanje (breathing):** `BreathingExercise` — a themed circle that expands
  (Udahni) and contracts (Izdahni) on a 4-second cycle, silent, with a "Završi" exit.
- **Mir → Uzemljavanje (grounding):** `GroundingExercise` — the 5-4-3-2-1 technique, one
  plain prompt at a time, tap "Dalje" to advance.
- **Mir → Pozovi podršku:** dials the SOS line 0800 100 600.
- **Uči:** the 5 education topics now open readable, plain-language article screens
  (Serbian) with back navigation. Content covers grooming, forms of abuse, digital
  safety, what to do if something happens (incl. 192 / 194 / 112 / SOS), and how to help
  someone. Implemented in `suncica/MirUci.kt`.

---

## 6. "OP" red-error diagnosis (AppOps)

Question: why did Android Studio show red on the AppOps code?

**Root cause:** Android Studio compiles against the public `android.jar` SDK stub, which
**omits everything marked `@hide` / `@SystemApi` / `@UnsupportedAppUsage`.** The "who used
location/mic in the last 5 minutes" feature in `scan/LocationAudit.kt` inherently needs
hidden framework APIs:

- `AppOpsManager.getPackagesForOps(String[])` — the `String[]` overload is hidden
  (the public one takes `int[]`).
- `AppOpsManager.PackageOps` and `AppOpsManager.OpEntry` inner classes — `@hide`
  → "Cannot resolve symbol".
- `OP_FLAG_SELF` / `OP_FLAG_TRUSTED_PROXY` / `OP_FLAG_TRUSTED_PROXIED` — `@hide` flags.
- `OpEntry.getLastAccessTime(int)` — `@hide`.

These are exactly what the recent commits already fixed: **reflection** for the hidden
methods/classes (no direct type references) and **hardcoded ints** `0x1 or 0x2 or 0x8`
for the flags.

**Verified:** every `OPSTR_*` constant used in `LocationAudit.kt`
(`OPSTR_FINE_LOCATION`, `OPSTR_COARSE_LOCATION`, `OPSTR_RECORD_AUDIO`, `OPSTR_CAMERA`,
`OPSTR_READ_CONTACTS`, `OPSTR_READ_CALL_LOG`, `OPSTR_READ_SMS`, `OPSTR_BODY_SENSORS`,
`OPSTR_PROCESS_OUTGOING_CALLS`) is **public** in the AOSP `AppOpsManager` source — so the
op-string constants were never the cause and need no change.

If it is still red locally: do **File → Sync Project with Gradle Files**, then
**Build → Clean Project**, and if needed **File → Invalidate Caches / Restart**. Confirm
`compileSdk` is recent (these APIs need 23+, which the project already targets).

---

## 7. Files added / changed this session

Added:
- `app/src/main/java/com/smiraj/meditation/weather/WeatherScreen.kt`
- `app/src/main/java/com/smiraj/meditation/weather/SuncicaUi.kt`
- `app/src/main/java/com/smiraj/meditation/suncica/SafeApp.kt`
- `app/src/main/java/com/smiraj/meditation/suncica/MirUci.kt`

Changed:
- `app/src/main/java/com/smiraj/meditation/MainActivity.kt` (cover → weather, route to SafeApp)
- `app/src/main/java/com/smiraj/meditation/AppState.kt` (added `Screen.SafeApp`)
- `app/src/main/java/com/smiraj/meditation/AppViewModel.kt` (`enterDiagnostics` → SafeApp, `startScan`)
- `app/src/main/res/values/strings.xml` (`app_name` → Sunčica)
- `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml`, `values/colors.xml`
- Restored: `scan/AccountAudit.kt`, `scan/PackageScanner.kt`, `scan/SpecialAccessChecker.kt`

---

## 8. How to test

1. Build & run. The launcher icon and app name show **Sunčica**.
2. App opens to the **weather screen** (Belgrade forecast).
3. **Double-tap the temperature** → hidden 5-tab safety app opens (SOS tab first).
4. Tap **Sken → Skeniraj sada** to run the real device scan; findings appear themed.
5. **Mir → Disanje** animates breathing; **Uzemljavanje** runs 5-4-3-2-1; **Pozovi podršku** dials.
6. **Uči** → tap any topic to read the article; "‹ Nazad" returns.
7. **System back** from anywhere in the hidden app = panic exit back to the weather screen.

---

## 9. Known limitations / next steps

- SOS button and the Mapa map are faithful UI from the mockup, **not yet wired to real
  backends** (SMS-to-contacts, live GPS, Mapbox). The spec puts these in later phases.
- Weather data is static mock (by design). Swap to OpenWeatherMap later if desired.
- Old meditation screens and the deep "Leči/Seči" report flow still exist in the codebase
  but are no longer wired into the primary navigation; can be removed for a clean tree.
- Build was verified statically (this environment has no Android SDK). Run a Gradle build
  in Android Studio to confirm green.
---

## 10. Hackathon demo updates — PIN, SOS, map, widget

Implemented for the final hackathon flow:

- **Launch PIN gate:** app now opens to a PIN screen. PIN `0` enters the real hidden
  Sunčica app. Any other non-empty PIN opens the functional weather decoy.
- **Weather decoy:** the old double-tap temperature entry was removed from routing; the
  fake app is now reached through the wrong-PIN path.
- **SOS hold flow:** the hidden SOS tab now uses a real press/release interaction on the
  emergency ring. Releasing it opens a 5-second cancel screen. PIN `0` cancels; any other
  input or timeout moves to a false black screen.
- **Interactive mock risk map:** added clickable mock safety zones for Belgrade areas,
  search by district/place, selected-zone details, risk level, and safe-route advice.
  This is functional demo data, not Mapbox/live backend data.
- **Android widget:** added a home-screen widget with weather cover styling. Tapping the
  widget opens the app/PIN gate; `112` opens the emergency dialer; `SOS` opens the ASTRA
  support dialer (`0800 100 600`).

Verification note:

- `gradlew.bat` is missing from the repo and local `gradle` is not on PATH, so CLI build
  could not be run in this shell. Run `assembleDebug` from Android Studio, or add the
  Gradle wrapper and run `./gradlew assembleDebug`.

---

## 11. Emergency SMS contact

Added a central manual emergency contact config:

- `emergency/EmergencyContact.kt` stores the contact name, phone number, and SMS body.
  Replace `PHONE` with the real trusted contact before demo/use.
- `emergency/EmergencySms.kt` sends that SMS through Android `SmsManager`.
- Manifest now requests `SEND_SMS`.
- Hidden SOS contact list now includes a direct `SMS` row for the emergency contact.
  The first tap requests SMS permission if needed; after permission is granted it sends
  the configured SOS message.
- SOS timeout flow now calls the same SMS sender before moving to the false black screen.
- Widget `SMS` button uses the same emergency contact. If SMS permission is already
  granted, it sends directly; otherwise it opens the SMS composer with the contact and
  message filled in.

---

## 12. Emergency contact import + scan history

Added follow-up fixes/features:

- Entering the hidden app now requests `SEND_SMS`, `CALL_PHONE`, and `READ_CONTACTS`
  permissions up front, so SOS does not need to request permissions during panic flow.
- Emergency contact is now stored via `EmergencyContactStore`; widget and in-app SOS read
  the same selected contact.
- SOS tab can load device contacts and select/import one as the active emergency contact.
- The selected emergency contact can be called directly when `CALL_PHONE` is granted;
  otherwise the app falls back to the dialer.
- SOS SMS send is wrapped so denied/missing SMS capability does not crash the app.
- Countdown cancel screen now shows a numpad and says to enter the user's PIN, without
  revealing that the demo cancel PIN is `0`.
- Sken tab now has a working in-memory scan history log for the latest scan snapshots.
- Account checking remains intentionally unimplemented; the UI now describes the planned
  approach instead of pretending it is functional.
