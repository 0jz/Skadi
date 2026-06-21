package com.smiraj.meditation.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Guided location-exposure findings and a coarsened-location message template.
 *
 * Invariant: no data is written to disk, Room, or DataStore.
 * Cleared on exitToCover() via LeciReport.demo().
 */
object LocationAudit {

    fun demoFindings(): List<LocationFinding> = listOf(
        LocationFinding(
            label = "Google Maps — deljenje lokacije u realnom vremenu",
            description = "Aktivno deljenje lokacije može biti uključeno bez vidljivog obaveštenja. " +
                "Proverite ko ima pristup i na koliko dugo.",
            severity = FindingSeverity.High,
            actionUrl = "https://maps.google.com/sharing",
        ),
        LocationFinding(
            label = "Google Family Link / porodična grupa",
            description = "Porodična grupa na Google nalogu može videti lokaciju u realnom vremenu " +
                "ako je Family Link ili zajednički Google Calendar aktivan.",
            severity = FindingSeverity.High,
            actionUrl = "https://families.google.com/families",
        ),
        LocationFinding(
            label = "Find My Device (Google)",
            description = "Svako ko ima pristup Google nalogu može videti poziciju uređaja putem " +
                "Find My Device. Proverite koje sesije su aktivne na nalogu.",
            severity = FindingSeverity.High,
            actionUrl = "https://myaccount.google.com/find-your-phone",
        ),
        LocationFinding(
            label = "Apple Find My / Porodično deljenje",
            description = "Ako koristite Apple uređaj i Porodično deljenje, ostali članovi grupe " +
                "mogu videti vašu lokaciju u aplikaciji Find My.",
            severity = FindingSeverity.High,
            actionUrl = "https://support.apple.com/en-us/HT210400",
        ),
        LocationFinding(
            label = "Mobilni operater — praćenje poziva",
            description = "Neke porodične tarife uključuju pregled poziva i podataka o lokaciji " +
                "za sve linije na računu. Proverite ko ima vlasnički pristup ugovoru.",
            severity = FindingSeverity.High,
            actionUrl = "https://www.mts.rs/privatni-korisnici/",
        ),
    )

    // Ordered map: OPSTR → Serbian label. Duplicate labels collapse in output.
    private val OP_LABELS = linkedMapOf(
        AppOpsManager.OPSTR_FINE_LOCATION          to "Lokacija",
        AppOpsManager.OPSTR_COARSE_LOCATION        to "Lokacija",
        AppOpsManager.OPSTR_RECORD_AUDIO           to "Mikrofon",
        AppOpsManager.OPSTR_CAMERA                 to "Kamera",
        AppOpsManager.OPSTR_READ_CONTACTS          to "Kontakti",
        AppOpsManager.OPSTR_READ_CALL_LOG          to "Istorija poziva",
        AppOpsManager.OPSTR_READ_SMS               to "Poruke (SMS)",
        AppOpsManager.OPSTR_BODY_SENSORS           to "Senzori tela",
        AppOpsManager.OPSTR_PROCESS_OUTGOING_CALLS to "Odlazni pozivi",
    )

    /**
     * Returns one [PermUsage] per permission with at least one app that accessed it
     * in the last [windowMinutes] minutes.
     *
     * On API 29+ uses AppOpsManager.getPackagesForOps via reflection for accurate
     * last-access timestamps — correctly detects WhatsApp live-location and other
     * background permission usage. Falls back to process scan on older APIs.
     */
    fun activeAllPermApps(context: Context, windowMinutes: Int = 5): List<PermUsage> {
        val windowMs = System.currentTimeMillis() - windowMinutes * 60_000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try { return activeAllViaAppOps(context, windowMs) } catch (_: Exception) { }
        }
        return activeAllViaProcesses(context)
    }

    /**
     * Accurate AppOps path (API 29+).
     * PackageOps and OpEntry are both @hide so we use reflection exclusively —
     * no direct type references to either inner class.
     */
    @SuppressLint("NewApi")
    private fun activeAllViaAppOps(context: Context, windowMs: Long): List<PermUsage> {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val pm = context.packageManager

        val getPackagesForOps = AppOpsManager::class.java
            .getMethod("getPackagesForOps", Array<String>::class.java)

        val queryOps: Array<String> = OP_LABELS.keys.toTypedArray()
        @Suppress("UNCHECKED_CAST")
        val packages = getPackagesForOps.invoke(appOps, queryOps as Any) as? List<*>
            ?: return emptyList()

        val labelToApps = mutableMapOf<String, MutableSet<String>>()
        val flags = AppOpsManager.OP_FLAG_SELF or
            AppOpsManager.OP_FLAG_TRUSTED_PROXY or
            AppOpsManager.OP_FLAG_TRUSTED_PROXIED

        for (rawPkg in packages) {
            rawPkg ?: continue
            val pkgCls = rawPkg.javaClass
            val pkg = pkgCls.getMethod("getPackageName").invoke(rawPkg) as? String ?: continue
            if (pkg == context.packageName) continue

            @Suppress("UNCHECKED_CAST")
            val ops = pkgCls.getMethod("getOps").invoke(rawPkg) as? List<*> ?: continue

            for (rawEntry in ops) {
                rawEntry ?: continue
                val entryCls = rawEntry.javaClass

                val lastAccess = entryCls
                    .getMethod("getLastAccessTime", Int::class.javaPrimitiveType)
                    .invoke(rawEntry, flags) as? Long ?: 0L
                if (lastAccess <= windowMs) continue

                val opStr = entryCls.getMethod("getOpStr").invoke(rawEntry) as? String ?: continue
                val label = OP_LABELS[opStr] ?: continue

                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (_: Exception) { pkg }

                labelToApps.getOrPut(label) { mutableSetOf() }.add(appName)
            }
        }

        val seen = mutableSetOf<String>()
        return OP_LABELS.values.mapNotNull { label ->
            if (!seen.add(label)) return@mapNotNull null
            val apps = labelToApps[label]?.sorted() ?: return@mapNotNull null
            PermUsage(label, apps)
        }
    }

    /** Fallback for API < 29: running foreground processes + permission check. */
    private fun activeAllViaProcesses(context: Context): List<PermUsage> {
        val permLabels = linkedMapOf(
            Manifest.permission.ACCESS_FINE_LOCATION   to "Lokacija",
            Manifest.permission.RECORD_AUDIO           to "Mikrofon",
            Manifest.permission.CAMERA                 to "Kamera",
            Manifest.permission.READ_CONTACTS          to "Kontakti",
            Manifest.permission.READ_CALL_LOG          to "Istorija poziva",
            Manifest.permission.READ_SMS               to "Poruke (SMS)",
            Manifest.permission.BODY_SENSORS           to "Senzori tela",
            Manifest.permission.PROCESS_OUTGOING_CALLS to "Odlazni pozivi",
        )
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val fgPkgs = am.runningAppProcesses
                ?.filter { it.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE }
                ?.flatMap { it.pkgList?.toList() ?: emptyList() }
                ?.filter { it != context.packageName }
                ?.distinct()
                ?: emptyList()

            permLabels.mapNotNull { (perm, label) ->
                val apps = fgPkgs
                    .filter { pm.checkPermission(perm, it) == PackageManager.PERMISSION_GRANTED }
                    .mapNotNull { pkg ->
                        try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }
                        catch (_: Exception) { null }
                    }
                    .sorted()
                if (apps.isNotEmpty()) PermUsage(label, apps) else null
            }
        } catch (_: Exception) { emptyList() }
    }

    /**
     * Returns apps that accessed location in the last [windowMinutes] minutes.
     * Used by the Location section of the Leči report.
     */
    fun activeLocationApps(context: Context, windowMinutes: Int = 5): List<String> {
        val windowMs = System.currentTimeMillis() - windowMinutes * 60_000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try { return activeLocationViaAppOps(context, windowMs) } catch (_: Exception) { }
        }
        return activeLocationViaProcesses(context)
    }

    @SuppressLint("NewApi")
    private fun activeLocationViaAppOps(context: Context, windowMs: Long): List<String> {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val pm = context.packageManager
        val method = AppOpsManager::class.java
            .getMethod("getPackagesForOps", Array<String>::class.java)
        val locationOps: Array<String> = arrayOf(
            AppOpsManager.OPSTR_FINE_LOCATION,
            AppOpsManager.OPSTR_COARSE_LOCATION,
        )
        @Suppress("UNCHECKED_CAST")
        val packages = method.invoke(appOps, locationOps as Any) as? List<*> ?: return emptyList()
        val found = mutableSetOf<String>()
        val flags = AppOpsManager.OP_FLAG_SELF or
            AppOpsManager.OP_FLAG_TRUSTED_PROXY or
            AppOpsManager.OP_FLAG_TRUSTED_PROXIED
        for (rawPkg in packages) {
            rawPkg ?: continue
            val pkgCls = rawPkg.javaClass
            val pkg = pkgCls.getMethod("getPackageName").invoke(rawPkg) as? String ?: continue
            if (pkg == context.packageName) continue
            @Suppress("UNCHECKED_CAST")
            val ops = pkgCls.getMethod("getOps").invoke(rawPkg) as? List<*> ?: continue
            for (rawEntry in ops) {
                rawEntry ?: continue
                val entryCls = rawEntry.javaClass
                val lastAccess = entryCls
                    .getMethod("getLastAccessTime", Int::class.javaPrimitiveType)
                    .invoke(rawEntry, flags) as? Long ?: 0L
                if (lastAccess > windowMs) {
                    try { found.add(pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()) }
                    catch (_: Exception) { found.add(pkg) }
                }
            }
        }
        return found.sorted()
    }

    private fun activeLocationViaProcesses(context: Context): List<String> {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            am.runningAppProcesses
                ?.filter { it.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE }
                ?.flatMap { it.pkgList?.toList() ?: emptyList() }
                ?.distinct()
                ?.filter { pkg ->
                    pkg != context.packageName && (
                        pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, pkg) == PackageManager.PERMISSION_GRANTED ||
                        pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, pkg) == PackageManager.PERMISSION_GRANTED
                    )
                }
                ?.mapNotNull { pkg ->
                    try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }
                    catch (_: Exception) { null }
                }
                ?.sorted()
                ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun coarsenedMessage(): String =
        "Trenutno sam u centru grada i u redu sam. Javim ti se večeras do 20h."
}
