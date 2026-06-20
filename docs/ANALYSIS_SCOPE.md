# Analysis scope - Full app

Branch: `analysis/full-app`

## Scope

This branch analyzes the whole Skadi app against all six shared principles:

1. Safety and privacy first
2. Simple and fast use
3. Multiple ways to reach help
4. Connect to existing support systems
5. Local context and understandable language
6. Realistic and buildable solution

## Current app coverage

Already present:

- Smiraj meditation cover.
- Secret code entry.
- Hidden Diagnostics/Safety screens.
- `FLAG_SECURE`.
- Panic exit to cover.
- Leči/Seči prototype.
- Planning docs for real scanner and pitch.

Still to build:

- Real app scanner.
- Special-access preflight.
- Leči as structured report.
- Guided account/password audit.
- Location/family audit.
- Device audit.
- Seči strength engine.
- Test fixture app and fake datasets.

## Principle analysis

### 01 - Safety and privacy first

Keep:

- no hidden-state persistence
- no scanner findings in Room/DataStore
- hidden screens protected
- no automatic destructive actions
- optional encrypted imports only

Watch:

- password CSV handling
- export files
- support messages that could be seen by attacker

### 02 - Simple and fast use

Keep:

- secret code as quick access
- Leči report as first hidden state
- one primary next step at a time

Watch:

- account audit must not become a huge form
- Seči strengths must be clear and few

### 03 - Multiple ways to reach help

Add:

- trusted-person SMS draft
- trusted-person call
- ASTRA/AŽC dialer links
- neutral message copy
- offline "what to say" scripts

Avoid:

- automatic SMS
- silent calls
- location broadcasting

### 04 - Connect to support systems

Add:

- specialized support contacts behind safety gate
- scripts for talking to support
- prep checklist before calling
- support suggestion per Seči strength

Avoid:

- pretending to replace support organizations
- chatbot as first MVP dependency

### 05 - Local context and understandable language

Keep:

- Serbian UI.
- Neutral Diagnostics language.
- Direct safety language only behind gate.

Add:

- low-jargon report explanations
- local support names/numbers
- local examples for location/family/account risk

### 06 - Realistic and buildable

Build now:

- Android package/permission scanner.
- Special-access preflight where possible.
- Fake CSV/demo guided account audit.
- Offline password generation.
- Strength-based Seči checklist.

Build later:

- encrypted import/export.
- HIBP checks with consent.
- provider-specific account guides.
- IOC matching.
- regional support directory.

Do not promise:

- automatic password changes.
- deleting all traces of Google export/download history.
- silent session revocation across arbitrary services.
- safe attacker-facing GPS spoofing.
- reading other apps' screens.

## Branch layering rule

Every added layer gets its own branch. Do not bundle multiple product risks in one branch.

Required order:

1. `feature/real-scanner-heal-report`
2. `feature/real-scanner-app-scanner`
3. `feature/real-scanner-special-access`
4. `feature/real-scanner-account-audit`
5. `feature/real-scanner-location-family`
6. `feature/real-scanner-device-audit`
7. `feature/real-scanner-cut-light`
8. `feature/real-scanner-cut-planned`
9. `feature/real-scanner-cut-strong`
10. `feature/real-scanner-test-fixture`
11. `feature/real-scanner-demo-docs`

Each branch should update:

- `PROGRESS.md`
- relevant docs
- implementation only for that layer

Each branch should pass:

- `assembleDebug`
- manual hidden-flow smoke test

## Mentor-facing summary

Skadi should be pitched as:

- not an antivirus
- not spyware against spyware
- not primarily a panic alarm
- a survivor-centered anti-tracking workflow:
  detect, explain, involve support, and act in the right order.
