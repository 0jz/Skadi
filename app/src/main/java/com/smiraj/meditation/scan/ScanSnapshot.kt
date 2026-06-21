package com.smiraj.meditation.scan

// PermUsage is defined in LeciReport.kt (same package)

data class ScanSnapshot(
    val findings: List<Finding>,
    val ranAtMillis: Long,
    /** BLE trackers found during the 10-second scan. Empty until scan completes. */
    val bleTrackers: List<BleTrackerFinding> = emptyList(),
    /** True once the BLE scan has finished (or was skipped due to unavailability). */
    val bleScanned: Boolean = false,
    /** Permissions actively in use by foreground apps, wi