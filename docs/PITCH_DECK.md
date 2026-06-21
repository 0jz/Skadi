# Skadi pitch deck outline

Purpose: concise deck skeleton for the SheSafe Hackathon pitch. Keep the story simple: detection is useful only when it does not increase danger.

## Slide 1 - Title

Skadi

Discreet anti-tracking support hidden behind a real meditation app.

Visual:
- Smiraj cover screen.
- Small hint of secret diagnostics, not too explicit.

Speaker point:
- "Skadi helps a survivor understand how she may be monitored, then choose a safer next step."

## Slide 2 - Problem

Tech-facilitated abuse is not just spyware.

Points:
- Monitoring often happens through legitimate access: shared accounts, location sharing, family controls, sessions, device access.
- Detection without a plan can escalate danger.
- A one-tap "delete everything" product is unsafe.

## Slide 3 - Six principles

Map to the hackathon principles:

1. Safety and privacy first
2. Simple and fast
3. Multiple ways to reach help
4. Connect to existing support systems
5. Local context and understandable language
6. Realistic and buildable

Speaker point:
- "We use these as product constraints, not slogans."

## Slide 4 - Core insight

Leči before Seči.

Leči:
- Report mode.
- Understand risk.
- Do not change anything visible.
- Prepare support.

Seči:
- Action mode.
- Choose strength.
- Follow a safe order.
- Manual, confirmed steps.

Visual:
- Two paths after report: Leči stays quiet, Seči starts planned action.

## Slide 5 - Demo flow

1. Open Smiraj meditation.
2. Enter secret code in custom timer.
3. Preflight privacy check.
4. Leči report with four areas:
   - Apps
   - Accounts
   - Location/family
   - Device
5. Choose Seči strength.
6. Generate actions and support plan.

## Slide 6 - What works now

Current implemented state:

- Real meditation decoy.
- Secret trigger.
- `FLAG_SECURE`.
- Panic exit.
- Diagnostics and Safety screens.
- Leči/Seči prototype.

Next technical branch:
- Real scanner model and report engine.

## Slide 7 - Real scanner

Buildable Android checks:

- App requested permissions.
- Suspicious combinations.
- Apps without obvious launcher entry, where detectable.
- Accessibility / Notification Listener / Device Admin / Usage Access guided checks.
- Bluetooth and device audit where permissions allow.

Honest limit:
- Android package visibility limits mean we may not see everything on every device.

## Slide 8 - Account and password audit

Demo:
- Fake offline CSV.
- Generated new passwords.
- Simulated sessions/family risk.

Production:
- User-selected import.
- Offline processing.
- Android Keystore encrypted working copy.
- No automatic password changes.
- User-guided provider flows.

Speaker point:
- "We do not build spyware to fight spyware."

## Slide 9 - Support system

Multiple help paths:

- Trusted person SMS/call draft.
- ASTRA / AŽC support contacts.
- Emergency option only when appropriate.
- Offline scripts: what to say, what to prepare.

Support escalation by Seči strength:
- Slabo: trusted person.
- Planirani izlazak: trusted person plus specialized support.
- Jako: support before destructive migration.
- Emergency: police only when safety context demands it.

## Slide 10 - Privacy and safety design

Rules:

- Findings stay in memory by default.
- No secret-state persistence.
- No automatic SMS/calls.
- No auto-delete.
- No cloud upload of password CSV.
- Sensitive imports are optional, local, encrypted, and cleared.
- Neutral language before safety gate.

## Slide 11 - Roadmap

Near term:
- Real app scanner.
- Special access preflight.
- Leči report engine.
- Seči strength engine.
- Test fixture app.

Later:
- Better IOC matching.
- HIBP checks with consent.
- Encrypted export bundle.
- More Serbian/local support resources.
- Provider-specific guided flows.

## Slide 12 - Close

Skadi turns a risky discovery into a safer decision.

One-liner:
- "It is not an antivirus. It is a safety plan hidden in plain sight."

Ask:
- Mentorship on survivor support flows.
- Validation with local organizations.
- Support for real-device testing.
