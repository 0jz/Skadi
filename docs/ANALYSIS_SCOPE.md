# Analysis scope - Principles 01-02

Branch: `analysis/principles-01-02`

## Scope

This branch analyzes the first two shared principles:

1. Safety and privacy first
2. Simple and fast use

## What to evaluate

Safety and privacy:

- Hidden entry should not create a visible "secret app" affordance.
- Hidden screens should use `FLAG_SECURE`.
- Back/background should exit to the meditation cover.
- Sensitive scanner findings should stay in memory by default.
- Password/account imports must be optional, local, encrypted, and user-confirmed.

Speed and simplicity:

- Secret access should remain a short action: custom timer value -> hidden flow.
- The hidden flow should not start with many decisions.
- Leči should be the default first state: report and understanding.
- Seči should appear only after safety gate and readiness.
- Emergency/support actions should be one tap away, but still user-confirmed.

## What should not be implemented on this branch

- No real scanner code.
- No CSV import.
- No support database.
- No destructive account/device actions.

## Layering rule

Every added layer should get its own branch and be merged in order.

Recommended order:

1. `feature/real-scanner-heal-report`
2. `feature/real-scanner-app-scanner`
3. `feature/real-scanner-special-access`
4. `feature/real-scanner-account-audit`
5. `feature/real-scanner-location-family`
6. `feature/real-scanner-device-audit`
7. `feature/real-scanner-cut-light`
8. `feature/real-scanner-cut-planned`
9. `feature/real-scanner-cut-strong`
