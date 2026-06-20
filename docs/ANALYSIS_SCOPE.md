# Analysis scope - Principle 01

Branch: `analysis/principle-01`

## Scope

This branch analyzes only the first shared principle:

1. Safety and privacy first

## What to evaluate

- Does the decoy meditation app avoid visible traces?
- Does the hidden layer avoid persistent secret-state storage?
- Are scanner findings kept out of Room/DataStore by default?
- Does `FLAG_SECURE` protect hidden screens?
- Does panic exit return to the cover without leaving hidden context visible?
- Are password/account imports treated as sensitive and optional?

## What should not be implemented on this branch

- No real scanner code.
- No account import.
- No Leči/Seči UI changes.
- No support/contact expansion.

## Layering rule

Every added product or technical layer should get its own branch before it is merged into `main`.

Suggested follow-up branches:

- `feature/real-scanner-heal-report` for the Leči report layer.
- `feature/real-scanner-app-scanner` for real app permission scanning.
- `feature/real-scanner-special-access` for Accessibility/Notification/Device Admin checks.
- `feature/real-scanner-account-audit` for guided account and password audit.
