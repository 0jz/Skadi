package com.smiraj.meditation

/**
 * Top-level screen for the single-Activity app.
 *
 * Hidden-layer navigation is in-memory only. It is deliberately not persisted
 * and not represented as a system navigation back stack, so a panic exit can
 * always drop directly back to the meditation cover.
 */
enum class Screen {
    Meditation,
    Diagnostics,
    SafetyGate,
    /** Leči/Seči report. Shown only after preflight passes. */
    Safety,
    /**
     * Shown when preflight detects a blocking accessibility risk.
     * Displays a neutral "unavailable" message — no DV language, no report.
     * Returns to cover on any action.
     */
    PreflightBlocked,
}
