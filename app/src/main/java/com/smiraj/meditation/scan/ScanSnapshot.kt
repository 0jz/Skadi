package com.smiraj.meditation.scan

data class ScanSnapshot(
    val findings: List<Finding>,
    val ranAtMillis: Long,
    /** BLE trackers found during the 10-second scan. Empty until scan completes. */
    val bleTrackers: List<BleTrackerFinding> = emptyList(),
    /** True once the BLE scan has finished (or was skipped due to unavailability). */
    val bleScanned: Boolean = false,
) {
    companion object {
        fun empty(): ScanSnapshot = ScanSnapshot(
            findings = emptyList(),
