package com.smiraj.meditation.scan

/**
 * Structured Leči report with four security sections.
 *
 * All sections start in a "not ready" / demo state.
 * Real data is filled in by scanner and audit feature branches.
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
         * Empty demo report used as the zero-state.
         * Each section is marked as not ready; the UI shows import buttons / placeholders.
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

/** One entry per permission that has at least one active foreground app. */
data class PermUsage(val label: String, val apps: List<String>)

data class AppsSection(
    val findings: List<Finding>,
    /** Permissions currently in active use, with the apps holding them. */
    val activePermUsage: List<PermUsage> = emptyList(),
    val ready: Boolean,
)

// ---- Section 2: Accounts, passwords, sessions ------------------------------

data class AccountsSection(
    val entries: List<AccountEntry>,
    val ready: Boolean,
)

/**
 * A single account entry built from a CSV import or demo data.
 *
 * Sensitive fields ([username], [suggestedPassword]) are in memory only;
 * cleared on exitToCover() via [LeciReport.demo].
 */
data class AccountEntry(
    val label: String,
    val siteUrl: String,
    /**
     * Direct link to this provider's security / active-sessions page.
     * Opened from the Seči panel so the user can act manually.
     */
    val securityUrl: String = siteUrl,
    /**
     * Account username or email — shown masked in the Leči report.
     * Null when the entry was built without CSV data.
     */
    val username: String? = null,
    val riskReasons: List<String>,
    val severity: FindingSeverity,
    /**
     * Offline-generated replacement password. null when the current password
     * is already strong. Generated with SecureRandom; never leaves the device.
     */
    val suggestedPassword: String? = null,
)

// ---- Section 3: Location and family sharing --------------------------------

data class LocationSection(
    /**
     * Apps that have accessed location in the last 5 minutes (via AppOpsManager).
     * Shown at the top of the location section as the highest-priority risk.
     */
    val activeLocationApps: List<String> = emptyList(),
    /** All apps that have location permission (from package scanner). */
    val appsWithLocation: List<String>,
    /** Guided findings for shared-account / family-sharing location exposure. */
    val familyFindings: List<LocationFinding> = emptyList(),
    /**
     * Approximate-location message the user can send to a trusted person.
     * Template only — user edits before sending.
     */
    val coarsenedMessage: String? = null,
    val ready: Boolean,
)

// ---- Section 4: Device and physical access ---------------------------------

data class DeviceSection(
    val checkItems: List<DeviceCheckItem>,
    val ready: Boolea