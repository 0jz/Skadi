package com.smiraj.meditation.scan

data class ScanSnapshot(
    val findings: List<Finding>,
    val ranAtMillis: Long,
) {
    companion object {
        fun empty(): ScanSnapshot = ScanSnapshot(emptyList(), 0L)
    }
}

data class Finding(
    val appName: String,
    val packageName: String,
    val severity: FindingSeverity,
    val signals: List<String>,
    val neutralSummary: String,
    val safetySummary: String,
)

enum class FindingSeverity {
    Low,
    Medium,
    High,
}
