# Skadi - technical brief for mentors

Purpose: short explanation of how Skadi works technically, what "Leči" and "Seči" mean in implementation terms, and how the app can support urgent situations without becoming a panic-alert product.

## One-sentence summary

Skadi is a native Android anti-tracking assistant hidden behind a real meditation app. It detects practical monitoring risks, explains them in a safe order, and guides the user through either quiet preparation (`Leči`) or deliberate cleanup (`Seči`).

## Why native Android

The core detection work cannot be done well as a web app.

Native Android gives access to:

- installed/visible package metadata through `PackageManager`
- requested app permissions
- special-access checks and settings guidance
- device admin information
- system intents for opening settings, dialer, SMS, browser, and provider security pages
- Android Keystore for local encryption of sensitive working data

No root access is used. No backend is required for the MVP.

## App structure

The visible app is `Smiraj`, a meditation cover:

- breathing timer
- session history
- ambient sound settings

The hidden layer is opened by a secret value in the custom timer field. The current default is `0`.

Secret-layer protections:

- `FLAG_SECURE` on hidden screens
- back button returns to cover
- app backgrounding returns to cover
- no secret-mode flag persisted to disk
- scanner findings stay in memory by default

## Leči: report and preparation mode

`Leči` is not an action mode. It is an informed, low-noise state.

Technical behavior:

- Run a scan or guided audit.
- Show a report with four categories.
- Explain what each finding may mean.
- Avoid uninstalling apps, revoking access, changing passwords, or signing out sessions.
- Prepare messages/checklists locally.
- Keep findings in memory unless the user explicitly exports an encrypted report later.

The report sections are:

1. Apps and dangerous access
2. Accounts and sessions
3. Location and family sharing
4. Device and physical access

### Apps and dangerous access

Methods:

- `PackageManager` for visible installed packages
- `PackageInfo.requestedPermissions`
- launcher intent checks, where available
- risk scoring for permission combinations

Examples of risky combinations:

- location + microphone
- location + SMS/calls
- camera + microphone
- app with sensitive permissions but no visible launcher entry

### Special-access preflight

Before showing the hidden report, Skadi should check for highly sensitive access:

- Accessibility services
- Notification listeners
- Device admin apps
- Usage access

If a suspicious Accessibility/interactive access risk is detected, the app should not show the secret report. It should leave the hidden flow with a neutral message such as:

> Provera privatnosti trenutno nije dostupna zbog podešavanja pristupačnosti.

This avoids displaying sensitive information while a potentially abusive service may be observing the screen.

### Accounts and sessions

Android apps cannot safely read a user's Google/Apple/Meta sessions or saved passwords directly. Skadi therefore uses guided audit and optional imports.

Demo:

- fake CSV with accounts, websites, sessions, family-owner risk, and breach flags
- generated replacement passwords
- simulated "sign out sessions" checklist

Production:

- user-selected CSV import through Android file picker
- local/offline processing
- encrypted working copy with Android Keystore
- plaintext only temporarily in memory
- no automatic password change
- user opens provider security page and manually changes password/signs out sessions

Skadi can show:

- account name
- website/security link
- risk reason
- suggested new password
- "change manually" step
- "sign out other sessions" step

### Location and family sharing

Skadi does not try to secretly falsify attacker-facing GPS. That can be detectable and dangerous.

Instead:

- show apps with location permissions
- guide user to check Google Maps / Apple / family sharing settings
- identify family-owner risk in demo/guided audit
- prepare a coarse location message for a trusted person

Coarsening means sharing an approximate area, not an exact address.

Example:

> I am safe for now. I am around [neighborhood/area], not sharing exact address. Please check on me at [time].

This is useful for support without exposing precise location.

### Device and physical access

Methods:

- device admin checks where available
- Bluetooth paired-device checks where permissions allow
- guided checklist for biometrics, SIM/operator account, browser sync, and trusted devices

Leči only shows risk and preparation steps.

## Seči: action mode

`Seči` starts only after the user confirms she is in a safer context.

It is not a one-tap cleanup. It is a sequenced plan.

Rules:

- every destructive action is separate
- no automatic account deletion
- no automatic password changing
- no automatic SMS/calls
- no silent system-setting changes
- support contact comes before high-risk changes

## Seči strengths

### Slabo

For low-noise cleanup.

Technical actions:

- prepare SMS/call to one trusted person
- generate new password for the most critical account
- guide manual session review
- guide manual permission reduction for one obvious risky app

No police default.
No account deletion.
No factory reset.

### Planirani izlazak

Default recommended mode.

Technical actions:

- prompt trusted contact
- suggest ASTRA/AŽC contact
- export important data
- generate new passwords for key accounts
- open account security pages
- user manually changes passwords
- user manually signs out other sessions
- user removes attacker-controlled recovery email/phone
- user leaves family/shared control if attacker owns the structure
- user removes suspicious apps after account safety is prepared

No factory reset by default.

### Jako

For compromised identity or attacker-controlled family/owner account.

Technical actions:

- export data first
- create new email/recovery/2FA base
- migrate essential accounts and contacts
- abandon or delete old accounts only after export and support check
- new device or factory reset is optional final step

### Nuklearno

For extreme compromise and only when physical safety/support exists.

Technical actions:

- same as `Jako`
- factory reset or new device at the end
- police option after safety preparation, or immediately if there is immediate danger

## Support and urgent situations

Skadi is not primarily a panic-button app. That overlaps with the first topic where the core feature is sending an alert.

Skadi can still support urgent situations in a limited, safe way:

- show emergency resources behind the safety gate
- open dialer with ASTRA/AŽC/police numbers
- prepare SMS drafts to trusted people
- copy neutral safety messages
- provide "what to say" scripts
- recommend calling specialized support before destructive account changes

Important distinction:

- Skadi does not automatically send messages.
- Skadi does not silently call anyone.
- Skadi does not broadcast location.

This keeps Skadi focused on anti-tracking and safe cleanup, while still giving fast access to support.

## Offline vs online

Offline MVP:

- app permission scanner
- special-access preflight where available
- fake account CSV demo
- password generation
- Leči report
- Seči plans
- support phone/SMS drafts

Optional online later:

- Have I Been Pwned email/password checks with explicit consent
- updated IOC lists
- support directory updates

Sensitive data should never be uploaded by default.

## Data handling

Default:

- findings in memory only
- cleared when leaving hidden flow or app process dies

For optional imports:

- use Android system picker
- import/copy into app-controlled working area only when necessary
- encrypt working copy with Android Keystore
- avoid logs
- wipe in-memory objects after processing where practical
- delete app-controlled working copy after completion

What Skadi cannot honestly guarantee:

- deleting browser download history
- deleting Google export audit history
- deleting a file outside app control
- changing passwords automatically across arbitrary services
- signing out all sessions automatically

## Demo scope

Realistic demo:

- working meditation cover
- secret entry
- hidden report
- test app detected as suspicious
- special-access preflight behavior
- fake password/account CSV
- generated new passwords
- Leči report
- Seči strength selection
- support plan

Later production:

- stronger scanner
- IOC list
- encrypted import/export
- provider-specific guided account flows
- optional breach checks
- regional support resources

## Mentor framing

Skadi is intentionally scoped:

- It is not spyware against spyware.
- It is not an antivirus.
- It is not a panic alarm.
- It is a survivor-centered anti-tracking workflow:
  detect risk, explain it safely, involve support, then act in the right order.
