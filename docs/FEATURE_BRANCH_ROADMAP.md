# Feature branch roadmap

Purpose: keep the real-scanner work small, ordered, and easy to merge. `main` stays the current working app. `integration/real-scanner-main` is the branch where feature branches can be merged and tested together before touching `main`.

## Branch roles

### `main`

Current stable demo app:

- meditation cover
- secret entry
- demo diagnostics
- safety gate
- Leči/Seči prototype
- planning documentation

Do not use `main` for experimental scanner work.

### `integration/real-scanner-main`

Integration copy of `main`.

Use it to merge feature branches in order:

```powershell
git switch integration/real-scanner-main
git merge feature/real-scanner-heal-report
git merge feature/real-scanner-app-scanner
```

When the integration branch is tested and stable, merge it into `main`.

## Required implementation order

### 0. Leči report foundation

Branch:

```text
feature/real-scanner-heal-report
```

Goal:

- Convert the current hidden safety screen into a real Leči report.
- Show four security sections even while data is still fake/demo.
- Keep report mode non-destructive.

Why first:

- Every scanner result needs a place to be displayed safely.
- Leči is the user's informed state before any Seči action.

Deliverables:

- Four report cards:
  - Apps and dangerous access
  - Accounts and sessions
  - Location and family sharing
  - Device and physical access
- Preflight result UI:
  - clear
  - blocked by risky accessibility/interactive access
  - needs guided audit
- No real destructive actions.

## Four prevention/security parts

These four parts must be added in order, because each later part depends on the report structure and user safety framing.

### 1. Apps and dangerous access

Branch:

```text
feature/real-scanner-app-scanner
```

Goal:

- Detect risky Android apps and permissions.

Methods:

- `PackageManager`
- visible installed packages
- requested permissions
- launcher intent presence where detectable
- risk scoring for permission combinations

Detect:

- location + microphone
- location + SMS/calls
- camera + microphone
- sensitive permissions on an app with weak/no visible launcher presence

Leči behavior:

- Explain risk.
- Do not uninstall.
- Do not change permissions automatically.

Seči input:

- Mark which apps should appear in cleanup steps later.

### 2. Special access and screen/control risk

Branch:

```text
feature/real-scanner-special-access
```

Goal:

- Detect or guide review of app access that can observe/control user activity.

Methods:

- accessibility settings/read path where available
- notification listener review
- device admin review
- usage access guidance

Detect/guide:

- Accessibility services
- Notification listeners
- Device admin apps
- Usage access apps

Special safety rule:

- If suspicious Accessibility/interactive access is active, do not show the hidden report.
- Exit to cover or neutral privacy-unavailable message.
- Do not say directly "someone is watching" on that screen.

Leči behavior:

- Tell user how to check these settings manually.
- Avoid changing anything yet.

Seči input:

- Add manual removal/revoke steps later.

### 3. Accounts, passwords, and sessions

Branch:

```text
feature/real-scanner-account-audit
```

Goal:

- Model account/session/password risks without pretending the app can read all real account data automatically.

MVP/demo:

- Fake CSV import.
- Fake list of accounts/sites.
- Fake active sessions and family-owner risk.
- Generated new passwords.
- Offline-only processing.
- Clear imported demo data on exit.

Production path:

- User-selected CSV through Android file picker.
- Local parsing.
- Android Keystore encrypted working copy.
- Plaintext only briefly in memory.
- No cloud upload by default.
- User manually changes passwords on provider sites.

Detect/represent:

- multiple sessions
- suspicious device/place
- weak/reused/breached password signal
- attacker-controlled recovery email/phone
- family/parental owner risk

Leči behavior:

- Show account risk report.
- Generate preparation tasks.
- Do not change passwords yet.

Seči input:

- Generate password-change checklist.
- Open provider security pages.
- User marks steps complete manually.

### 4. Location, family sharing, and device/physical access

This is split into two implementation branches so it stays manageable.

#### 4a. Location and family sharing

Branch:

```text
feature/real-scanner-location-family
```

Goal:

- Explain location exposure and family/shared-account risk.

MVP/demo:

- Show apps with location permission from the app scanner.
- Fake guided findings for Google/Apple/family sharing.
- Coarsened location message for trusted person.

Leči behavior:

- Explain who may see location.
- Prepare approximate-location message for trusted person.
- Do not spoof attacker-facing GPS.

Seči input:

- Manual review and disable steps.
- If attacker owns family account, prepare data export and account migration.

#### 4b. Device and physical access

Branch:

```text
feature/real-scanner-device-audit
```

Goal:

- Cover local device and physical-access risks.

MVP/demo:

- checklist for biometrics/PIN
- Bluetooth/trusted-device guidance
- SIM/operator account guidance
- browser sync guidance

Leči behavior:

- Show risks and prep steps.
- No removal yet.

Seči input:

- remove unknown trusted devices
- change PIN/biometrics
- review SIM/operator
- factory reset only as final optional step

## Seči strength branches

Add Seči strengths only after the four security parts exist, because each strength needs all four categories as input.

### Slabo Seči

Branch:

```text
feature/real-scanner-cut-light
```

Use case:

- Low-noise cleanup.

Support:

- trusted person only
- SMS/dialer draft, never automatic send/call

Actions:

- generate password for top critical account
- guide session review
- reduce one obvious risky permission manually
- no account deletion
- no police default

### Planirani izlazak

Branch:

```text
feature/real-scanner-cut-planned
```

Use case:

- Default recommended path, between medium and strong.

Support:

- trusted person first
- ASTRA/AŽC suggested before account/session migration

Actions:

- export key data
- generate passwords for important accounts
- change recovery email/phone manually
- sign out unknown sessions manually
- leave family/shared control if attacker owns it
- remove suspicious apps
- no factory reset by default

### Jako Seči

Branch:

```text
feature/real-scanner-cut-strong
```

Use case:

- Compromised identity or attacker-controlled family/owner account.

Support:

- specialized support strongly suggested before destructive migration

Actions:

- export data
- create new email/recovery/2FA base
- migrate essential accounts and contacts
- abandon/delete old accounts only after export and confirmation
- factory reset or new device only as optional final step
- police option only after safety prep or immediate danger path

## Test fixture and demo

### Test fixture

Branch:

```text
feature/real-scanner-test-fixture
```

Add after the scanner/report structure exists.

Goal:

- Provide repeatable test data and test apps.

Contents:

- suspicious "System Update" test app or fixture package
- high-risk requested permissions
- optional no-launcher variant
- fake password CSV
- fake account/session/family findings
- fake location/family findings

Rules:

- Must not perform spyware behavior.
- Must be debug/demo only.
- Must not ship in production release build.

### Demo docs

Branch:

```text
feature/real-scanner-demo-docs
```

Add last.

Goal:

- Explain demo flow and what is real vs simulated.

Docs:

- 3-minute demo script
- mentor explanation
- pitch talking points
- known limitations
- future production path

## Merge order into integration branch

Use this exact order:

```text
feature/real-scanner-heal-report
feature/real-scanner-app-scanner
feature/real-scanner-special-access
feature/real-scanner-account-audit
feature/real-scanner-location-family
feature/real-scanner-device-audit
feature/real-scanner-cut-light
feature/real-scanner-cut-planned
feature/real-scanner-cut-strong
feature/real-scanner-test-fixture
feature/real-scanner-demo-docs
```

## Completion checklist per branch

Before merge:

- update `PROGRESS.md`
- keep scope limited to the branch purpose
- avoid unrelated refactors
- keep hidden findings out of persistent storage
- no automatic SMS/calls/password changes/account deletion
- run `assembleDebug`
- manually smoke-test secret flow
- merge into `integration/real-scanner-main`, not directly into `main`

## When to update `main`

Only merge `integration/real-scanner-main` into `main` when:

- all merged branches build
- hidden flow works end-to-end
- demo flow is clear
- no debug fixture is included in production build
- docs explain what is real and what is simulated
