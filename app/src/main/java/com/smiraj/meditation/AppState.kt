package com.smiraj.meditation

/**
 * Top-level screen for the single-Activity app.
 *
 * Phase 2 (skeleton) implements only the cover (Meditation) and the empty,
 * neutral Diagnostics entry behind the magic-value trigger. SafetyGate and
 * Safety (Level 3) arrive in Phase 3 per SKADI_DESIGN_MEDITATION.md §3.
 *
 * This is held as in-memory state in [AppViewModel] — NOT in a system
 * Navigation back-stack — so a panic exit can drop the whole secret layer
 * instantly and `back` from the cover never reveals it.
 */
enum class Screen {
    Meditation,
    Diagnostics,
    // SafetyGate,  // Phase 3
    // Safety,      // Phase 3
}
