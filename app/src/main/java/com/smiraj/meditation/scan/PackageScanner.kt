package com.smiraj.meditation.scan

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Scans visible installed packages for risky permission combinations.
 *
 * Uses only Android framework APIs — no network, no file writes, no logs.
 * Results stay in memory; they are never written to Room, DataStore, or disk.
 *
 * Package visibility note:
 * Android 11+ filters which packages are visible to a normal app.
 * The manifest declares QUERY_ALL_PACKAGES for the demo build so the scanner
 * can see third-party apps. A production release should either justify this
 * permission to Google Play or replace it with targeted <queries> entries for
 * known stalkerware package patterns.
 */
class PackageScanner(private val context: Context) {

    /**
     * Runs the scan synchronously. Call from a background coroutine.
     * Returns a sorted list of risky findings, highest severity first.
     */
    fun scan(): List<Finding> {
        val pm = context.packageManager
        val findings = mutableListOf<Finding>()

        val installedApps = try {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            return emptyList()
        }

        // Packages that appear in the device launcher
        val launcherPackages: Set<String> = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            @Suppress("DEPRECATION") 0,
        ).map { it.activityInfo.packageName }.toSet()

        for (appInfo in installedApps) {
            val packageName = appInfo.packageName

            // Skip ourselves
            if (packageName == context.packageName) continue

            val requestedPermissions: Set<String> = try {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                    .requestedPermissions
                    ?.map { it.trim() }
                    ?.toSet()
                    ?: emptySet()
            } catch (_: PackageManager.NameNotFoundException) {
                continue
            } catch (_: Exception) {
                continue
            }

            val signals = detectSignals(requestedPermissions)
            if (signals.isEmpty()) continue

            val hasLauncher = packageName in launcherPackages
            if (!hasLauncher) {
                signals.add("Nema ikonu u pokretaču")
            }

            val severity = scoreSeverity(signals, hasLauncher)

            val appName = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                packageName
            }

            val summary = buildSummary(signals)

            findings.add(
                Finding(
                    appName = appName,
                    packageName = packageName,
                    severity = severity,
                    signals = signals.toList(),
                    neutralSummary = summary,
                    safetySummary = summary,
                )
            )
        }

        return findings.sortedByDescending { it.severity.ordinal }
    }

    // ---- Signal detection --------------------------------------------------

    private fun detectSignals(perms: Set<String>): MutableList<String> {
        val signals = mutableListOf<String>()

        val hasLocation   = LOCATION_PERMS.any { it in perms }
        val hasBgLocation = PERM_BG_LOCATION in perms
        val hasMic        = PERM_RECORD_AUDIO in perms
        val hasCamera     = PERM_CAMERA in perms
        val hasSms        = SMS_PERMS.any { it in perms }
        val hasCalls      = CALL_PERMS.any { it in perms }

        if (hasLocation && hasMic)   signals += "Lokacija + mikrofon"
        if (hasLocation && hasSms)   signals += "Lokacija + SMS"
        if (hasLocation && hasCalls) signals += "Lokacija + pozivi/log"
        if (hasCamera && hasMic)     signals += "Kamera + mikrofon"
        if (hasBgLocation)           signals += "Lokacija u pozadini"

        return signals
    }

    private fun scoreSeverity(signals: List<String>, hasLauncher: Boolean): FindingSeverity {
        val highKeywords = listOf(
            "Lokacija + mikrofon", "Lokacija + SMS",
            "Lokacija + pozivi/log", "Kamera + mikrofon",
        )
        return when {
            signals.any { it in highKeywords }   -> FindingSeverity.High
            !hasLauncher && signals.size >= 2    -> FindingSeverity.High
            !hasLauncher                         -> FindingSeverity.Medium
            signals.size >= 2                    -> FindingSeverity.Medium
            else                                 -> FindingSeverity.Low
        }
    }

    private fun buildSummary(signals: List<String>): String =
        "Kombinacija dozvola: ${signals.joinToString(", ")}."

    // ---- Permission constants ----------------------------------------------

    companion object {
        private val LOCATION_PERMS = setOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
        )
        private const val PERM_BG_LOCATION  = "android.permission.ACCESS_BACKGROUND_LOCATION"
        private const val PERM_RECORD_AUDIO = "android.permission.RECORD_AUDIO"
        private const val PERM_CAMERA       = "android.permission.CAMERA"
        private val SMS_PERMS = setOf(
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.RECEIVE_MMS",
        )
        private val CALL_PERMS = setOf(
            "android.permission.READ_CALL_LOG",
            "android.permission.PROCESS_OUTGOING_CALLS",
            "android.permission.READ_PHONE_STATE",
        )
    }
}
