# Analysis scope - Principles 01-03

Branch: `analysis/principles-01-03`

## Scope

This branch analyzes the first three shared principles:

1. Safety and privacy first
2. Simple and fast use
3. Multiple ways to reach help

## What to evaluate

Principles 01-02:

- Hidden flow should remain discreet, fast, and non-persistent.
- Report-first design should prevent impulsive destructive action.
- Seči should be gated and strength-based.

Principle 03:

- Help must be available in more than one form.
- The app should not rely only on emergency calls.
- Help actions should be user-confirmed, not automatic.

## Proposed help channels

For Leči:

- Copy a neutral message.
- Prepare SMS draft to a trusted person.
- Show "what to say" script.
- Keep ASTRA/AŽC contacts available behind the safety gate.

For Slabo Seči:

- Prompt one trusted person first.
- Offer SMS or dialer intent.
- No police as default.

For Planirani izlazak:

- Trusted person plus specialized support.
- ASTRA/AŽC suggested before account/session changes.

For Jako/Nuklearno:

- Specialized support before migration/deletion.
- Police option only after safety prep or immediate-danger path.

## What should not be implemented on this branch

- No automated SMS sending.
- No silent phone calls.
- No location broadcast.
- No backend chatbot.
- No cloud upload.

## Layering rule

Each added help/support layer should have its own branch.

Suggested implementation branches:

- `feature/real-scanner-heal-report` for report-first Leči.
- `feature/real-scanner-cut-light` for trusted-person support in low-strength Seči.
- `feature/real-scanner-cut-planned` for ASTRA/AŽC support in the default plan.
- `feature/real-scanner-cut-strong` for identity migration and final emergency options.
- `feature/real-scanner-demo-docs` for pitch/demo scripts.
