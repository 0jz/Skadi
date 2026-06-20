# Skadi Real Scanner - execution plan

Status: planning skeleton, no feature implementation yet.

Goal: keep `main` as the current working app, then add scanner and Leči/Seči capability through small branches that can be reviewed, tested, merged, and pitched independently.

## Source constraints checked

- Android package visibility is filtered from Android 11 onward, so a full installed-app inventory may require careful manifest `queries` or, for production, a justified `QUERY_ALL_PACKAGES` request. Source: https://developer.android.com/training/package-visibility
- `PackageManager` can inspect package metadata and requested permissions for visible packages. Source: https://developer.android.com/reference/android/content/pm/PackageManager
- `Settings.Secure` values can be read for user-controlled secure settings, but normal apps cannot write them. This fits "detect and guide", not "silently change". Source: https://developer.android.com/reference/android/provider/Settings.Secure
- Android Keystore can keep cryptographic keys non-exportable, which is the right base for local encrypted working copies. Source: https://developer.android.com/privacy-and-security/keystore
- Storage Access Framework should be used for user-selected imports/exports. Use `ACTION_GET_CONTENT` for one-time import/copy, `ACTION_OPEN_DOCUMENT` only when long-term access is needed. Source: https://developer.android.com/guide/topics/providers/document-provider
- Android common intents let us open dialer, SMS, browser, settings, and other apps. We should use user-confirmed dial/SMS flows, not silent calls/messages. Source: https://developer.android.com/guide/components/intents-common
- Have I Been Pwned supports Pwned Passwords k-anonymity and downloadable offline hash data. For MVP, use fake/offline demo data; for production, use consented checks only. Source: https://haveibeenpwned.com/api/v3

## Product shape

Leči is the report mode.

- It tells the user what is risky.
- It does not change anything externally.
- It avoids visible account/session/location changes.
- It can prepare a plan, copy neutral messages, and show support contacts.

Seči is the action mode.

- It starts only after a safety gate.
- It asks for a strength.
- It follows a safe order.
- It never batch-deletes or auto-changes accounts.
- It makes every destructive step user-confirmed and reversible where possible.

## Proposed branch sequence

All branches start from the latest `main`. Merge in this order.

1. `feature/real-scanner-heal-report`
   - Purpose: convert current Safety screen into "Leči = report".
   - Adds no real scanner yet.
   - Defines four report sections:
     - Apps and dangerous access
     - Accounts and sessions
     - Location and family sharing
     - Device and physical access
   - Adds preflight result states:
     - Clear
     - BlockedByAccessibilityRisk
     - NeedsGuidedAudit
   - Demo behavior: current fake findings become structured report cards.

2. `feature/real-scanner-app-scanner`
   - Purpose: real app/package scanner.
   - Reads visible installed packages and requested permissions.
   - Flags combinations:
     - Location + microphone
     - Location + SMS/calls
     - Camera + microphone + background behavior signal
     - App without launcher entry, when detectable
   - Notes:
     - Package visibility limits must be documented in-app.
     - For demo/test device, use a test fixture app to guarantee one finding.

3. `feature/real-scanner-special-access`
   - Purpose: preflight and high-risk access checks.
   - Detect/read where possible:
     - Enabled Accessibility services via secure settings / accessibility manager path
     - Notification listeners
     - Device admin apps
     - Usage access guidance
   - Behavior:
     - If suspicious accessibility/interactive access is active, do not show secret report.
     - Return to cover with neutral message:
       "Provera privatnosti trenutno nije dostupna zbog podešavanja pristupačnosti."
     - Next entry can guide user to manually check settings, without saying "someone is watching".

4. `feature/real-scanner-account-audit`
   - Purpose: guided account/password audit and offline demo import.
   - MVP/demo:
     - Fake CSV import fixture with accounts, sites, sessions, family-owner risk, breach flags.
     - Offline parsing.
     - Generate new passwords.
     - Show account, site link, risk, new suggested password.
     - Clear working data on exit.
   - Production path:
     - User picks CSV through system picker.
     - App copies/imports only what it needs.
     - Working copy encrypted with Android Keystore.
     - Plaintext exists only briefly in memory.
     - No network during CSV processing unless user explicitly enables breach checks.
     - App deletes its working copy, but does not promise it can erase browser download history or Google export logs.
   - Account actions remain guided:
     - Open security page.
     - User changes password manually.
     - User marks step complete.
     - User manually signs out other sessions through provider UI.

5. `feature/real-scanner-location-family`
   - Purpose: guided audit for location sharing and family/parental control.
   - MVP/demo:
     - Show apps with location permission from app scanner.
     - Fake location/family findings.
     - Coarsened location message for trusted person.
   - Leči:
     - Explain who may see location.
     - Prepare coarse location for a trusted person:
       "I am in this area, not exact address."
     - Do not mock/forge attacker-facing GPS.
   - Seči:
     - Check/disable location sharing manually.
     - If attacker controls family/owner account, plan data export and migration before leaving.

6. `feature/real-scanner-device-audit`
   - Purpose: device and physical access audit.
   - MVP/demo:
     - Checklist for biometrics, SIM/operator, browser sync.
     - Paired Bluetooth device list only where permissions allow.
   - Leči:
     - Show suspicious device/access signals.
     - No changes.
   - Seči:
     - Remove unknown trusted devices.
     - Change lock screen and biometrics.
     - Review SIM/operator account.
     - Factory reset only as final optional step.

7. `feature/real-scanner-cut-light`
   - Purpose: low-strength Seči.
   - Use when user wants minimal visible change.
   - Steps:
     - Contact one trusted person.
     - Change 1-3 critical passwords.
     - Review active sessions.
     - Remove only clearly dangerous app permissions.
   - No police default.
   - No account deletion.

8. `feature/real-scanner-cut-planned`
   - Purpose: default recommended Seči, between medium and strong.
   - Steps:
     - Contact trusted person and optionally ASTRA/AŽC.
     - Export important data.
     - Generate passwords for key accounts.
     - Change recovery email/phone.
     - Sign out unknown sessions.
     - Leave family/shared control if attacker is owner.
     - Remove suspicious apps.
   - No factory reset by default.

9. `feature/real-scanner-cut-strong`
   - Purpose: new-digital-identity path.
   - Steps:
     - Export data.
     - Create new email/recovery/2FA base.
     - Migrate contacts, photos, documents, essential services.
     - Abandon or delete compromised accounts only after export and support check.
     - Factory reset or new device is a final optional step.
   - Police appears only after safety prep or immediate danger path.

10. `feature/real-scanner-test-fixture`
    - Purpose: test package and fake datasets.
    - Adds a separate debug/test app or fixture data:
      - "System Update" app with high-risk requested permissions.
      - Optional no-launcher variant.
      - Fake password CSV.
      - Fake account/session/family findings.
    - Must not perform spyware behavior.

11. `feature/real-scanner-demo-docs`
    - Purpose: demo script, limits, and pitch polish.
    - Documents:
      - What is real now.
      - What is simulated.
      - Why automation is intentionally limited.
      - Privacy story.

## Merge checklist for every feature branch

- Branch starts from current `main`.
- Update `PROGRESS.md`.
- Keep findings and imported sensitive data out of Room/DataStore.
- No secret-mode flag in persistent storage.
- No automatic SMS, calls, account deletion, password changes, or permission changes.
- Neutral language before safety gate.
- Direct DV/support language only behind safety gate.
- `assembleDebug` must pass.
- If branch adds sensitive import/export, include a short threat-model note in docs.

## Leči flow, exact behavior

Entry:

1. User enters secret code.
2. App runs preflight.
3. If active suspicious accessibility/interactive-access risk is detected:
   - Do not show report.
   - Return to cover or show neutral privacy unavailable text.
   - Offer generic later guidance: "Proveri podešavanja pristupačnosti."
4. If preflight passes, show Leči report.

Leči report sections:

1. Apps and access:
   - Explain app permission combinations.
   - Show "what this could mean".
   - No uninstall/change buttons.

2. Accounts and sessions:
   - Show imported/demo accounts.
   - Show multi-session, suspicious place/device, family-owner, breach flags.
   - Generate "prepare later" tasks, not changes.

3. Location and family:
   - Show location permission apps.
   - Show guided audit for location sharing.
   - Generate coarse trusted-contact message.

4. Device:
   - Show special access, paired/trusted device hints, biometrics checklist.
   - No removal yet.

## Seči strengths, exact behavior

### Slabo

Use case: user wants a small, low-noise cleanup.

Support:
- Prompt SMS/call to one trusted person.
- No automatic send.

Actions:
- Generate new password for most critical account.
- Guide session review.
- Remove/reduce one clearly risky permission manually.

### Planirani izlazak (default)

Use case: user is preparing to leave or meaningfully secure accounts.

Support:
- Trusted person first.
- ASTRA/AŽC suggested before account migration.

Actions:
- Export key data.
- Generate passwords for important accounts.
- Open provider security pages.
- User changes manually and marks completion.
- Sign out suspicious sessions.
- Remove recovery contact controlled by attacker.
- Leave family/shared owner structure if attacker controls it.
- Remove suspicious apps.

### Jako

Use case: old identity is compromised or attacker controls family/owner account.

Support:
- Specialized support strongly suggested before destructive steps.

Actions:
- Export data.
- Create new email base.
- Move important contacts/data.
- Migrate accounts or abandon old ones.
- Delete old account only after export, support, and user confirmation.
- New device or factory reset is optional final step.

### Nuklearno

Use case: immediate high-risk compromise and user has physical safety/support.

Actions:
- Same as Jako.
- Factory reset or new device.
- Police option after safety plan or immediate danger.

## What is practical for demo

- Real app permission scanner.
- Real special-access preflight where Android allows reading.
- Fake account/password CSV.
- Fake session/family/location findings.
- Offline password generation.
- In-memory report cleared on exit.
- Support panel with dial/SMS/browser intents.

## What is practical later

- Local encrypted working copies with Android Keystore.
- Optional HIBP k-anonymity checks with consent.
- Offline downloaded Pwned Passwords hash range database for no-network checks.
- More provider-specific guided flows.
- Regional support directory.
- Encrypted export bundle for trusted person or legal support.

## What not to promise

- Automatic password changes across arbitrary services.
- Guaranteed removal of all Google/Chrome download/export traces.
- Silent deletion of accounts.
- Silent sign-out of all sessions.
- Secretly modifying system settings.
- Mocking attacker-facing GPS safely.
- Reading another app's screen or UI.
