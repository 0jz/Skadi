package com.smiraj.meditation.scan

/**
 * Structured Leči report with four security sections.
 *
 * All sections start in a "not ready" / demo state.
 * Real data is filled in by the scanner and audit feature branches.
 *
 * Invariant: findings and sensitive data stay in memory only.
 * They are never written to Room, DataStore, or files.
 */
data class LeciReport(
    val preflight: PreflightResult,
    val apps: AppsSection,
    val accounts: AccountsSection,
    val location: LocationSection,
    val device: DeviceSection,
) {
    companion object {
        /**
         * Empty demo report used until real scanner branches are merged.
         * Each section is marked as not ready so the UI shows guided placeholders.
         */
        fun demo(): LeciReport = LeciReport(
            preflight = PreflightResult.Clear,
            apps = AppsSection(findings = emptyList(), ready = false),
            accounts = AccountsSection(entries = emptyList(), ready = false),
            location = LocationSection(appsWithLocation = emptyList(), ready = false),
            device = DeviceSection(checkItems = emptyList(), ready = false),
        )
    }
}

// ---- Section 1: Apps and dangerous access ----------------------------------

/**
 * Populated by feature/real-scanner-app-scanner.
 * Detects: location+mic, location+SMS/calls, camera+mic, no-launcher apps.
 */
data class AppsSection(
    /** Scanner findings. Empty until feature/real-scanner-app-scanner. */
    val findings: List<Finding>,
    /** False until the real PackageManager scan runs. */
    val ready: Boolean,
)

// ---- Section 2: Accounts, passwords, sessions ------------------------------

/**
 * Populated by feature/real-scanner-account-audit.
 * MVP uses demo offline data. Production uses user-selected import + Keystore.
 */
data class AccountsSection(
    val entries: List<AccountEntry>,
    val ready: Boolean,
)

/**
 * A single account entry with risk signals and optional offline-generated
 * suggested password. Plaintext only briefly in memory; cleared on exit.
 */
data class AccountEntry(
    /** Human-readable name shown in the report (e.g. "Google nalog"). */
    val label: String,
    /** URL for the account's main page. */
    val siteUrl: String,
    /**
     * URL for the account's security/session settings page.
     * Opened from the Seči panel so the user can act manually.
     */
    val securityUrl: String = siteUrl,
    /** Risk signals shown as bullet points in the Leči card. */
    val riskReasons: List<String>,
    val severity: FindingSeverity,
    /**
     * Offline-generated password suggestion. null if no password change is
     * needed. Generated with SecureRandom; never leaves the device.
     */
    val suggestedPassword: String? = null,
)

// ---- Section 3: Location and family sharing --------------------------------

/**
 * Populated from AppsSection (apps with location permission) and guided audit.
 * Added in feature/real-scanner-location-family.
 */
data class LocationSection(
    /** App names that have location permission. Drawn from AppsSection when ready. */
    val appsWithLocation: List<String>,
    val ready: Boolean,
)

// ---- Section 4: Device and physical access ---------------------------------

/**
 * Populated by feature/real-scanner-device-audit.
 * Covers biometrics, Bluetooth trusted devices, SIM/operator, browser sync.
 */
data class DeviceSection(
    val checkItems: List<DeviceCheckItem>,
    val ready: Boolean,
)

data class DeviceCheckItem(
    val label: String,
    val guidance: String,
    val severity: FindingSeverity,
)
