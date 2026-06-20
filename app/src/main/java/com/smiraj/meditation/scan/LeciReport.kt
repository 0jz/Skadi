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
 * MVP uses a fake offline CSV. Production uses user-selected import + Keystore.
 */
data class AccountsSection(
    val entries: List<AccountEntry>,
    val ready: Boolean,
)

data class AccountEntry(
    val label: String,
    val siteUrl: String,
    val riskReasons: List<String>,
    val severity: FindingSeverity,
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
