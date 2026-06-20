package com.smiraj.meditation.scan

/**
 * Result of the preflight check run before showing the hidden Leči report.
 *
 * The check runs between SafetyGate confirmation and the Safety screen.
 * Real detection logic is added in feature/real-scanner-special-access.
 */
sealed class PreflightResult {

    /** No high-risk interference detected. Safe to show the report. */
    object Clear : PreflightResult()

    /**
     * Suspicious Accessibility or interactive-control access is active.
     * Do NOT show the hidden report — return to cover with a neutral message.
     * Avoid stating "someone is watching" on this path.
     */
    object BlockedByAccessibilityRisk : PreflightResult()

    /**
     * No confirmed blocking risk, but some special-access settings need manual
     * review. Show the report with a guided-audit banner.
     */
    object NeedsGuidedAudit : PreflightResult()
}
