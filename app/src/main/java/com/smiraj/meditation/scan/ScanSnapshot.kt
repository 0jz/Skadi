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

fun demoSnapshot(nowMillis: Long = System.currentTimeMillis()): ScanSnapshot =
    ScanSnapshot(
        findings = listOf(
            Finding(
                appName = "System Update",
                packageName = "com.system.update.service",
                severity = FindingSeverity.High,
                signals = listOf("Lokacija", "Mikrofon", "SMS", "Bez ikone"),
                neutralSummary = "Aplikacija ima vise osetljivih dozvola i nije vidljiva u launcheru.",
                safetySummary = "Ovakva kombinacija moze da se koristi za pracenje lokacije ili aktivnosti. Dokumentuj nalaz pre bilo kakvog uklanjanja.",
            ),
            Finding(
                appName = "Google Maps",
                packageName = "com.google.android.apps.maps",
                severity = FindingSeverity.Medium,
                signals = listOf("Lokacija", "Deljenje lokacije"),
                neutralSummary = "Proveri ko trenutno ima pristup tvojoj lokaciji.",
                safetySummary = "Legitimno deljenje lokacije je cest nacin pracenja. Proveri sa bezbednog mesta pre gasenja deljenja.",
            ),
        ),
        ranAtMillis = nowMillis,
    )
