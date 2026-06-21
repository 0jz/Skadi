package com.smiraj.meditation.scan

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * Checks for non-system apps that have powerful special access:
 *   - Accessibility services (can observe/control all screen content)
 *   - Notification listeners (can read all notifications)
 *   - Device admin apps (can enforce policies, remotely wipe)
 *   - Usage access (guided only — cannot be checked without the permission)
 *
 * No permissions required for the first three checks.
 * Results stay in memory only. Nothing is changed or written.
 */
class SpecialAccessChecker(private val context: Context) {

    data class Result(
        /** Non-system accessibility services currently enabled. High risk. */
        val a11yServices: List<AppInfo>,
        /** Non-system notification listeners currently enabled. Medium risk. */
        val notificationListeners: List<AppInfo>,
        /** Non-system active device admin apps. Medium risk. */
        val deviceAdmins: List<AppInfo>,
    ) {
        val hasBlockingRisk: Boolean get() = a11yServices.isNotEmpty()
        val hasGuidedAuditRisk: Boolean get() =
            notificationListeners.isNotEmpty() || deviceAdmins.isNotEmpty()
        val isEmpty: Boolean get() =
            a11yServices.isEmpty() && notificationListeners.isEmpty() && deviceAdmins.isEmpty()
    }

    data class AppInfo(val appName: String, val packageName: String)

    fun check(): Result {
        return Result(
            a11yServices = checkAccessibilityServices(),
            notificationListeners = checkNotificationListeners(),
            deviceAdmins = checkDeviceAdmins(),
        )
    }

    // ---- Accessibility services --------------------------------------------

    private fun checkAccessibilityServices(): List<AppInfo> {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return emptyList()
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .mapNotNull { info ->
                val pkg = info.resolveInfo.serviceInfo.packageName
                if (isSystemPackage(pkg)) null
                else AppInfo(appLabelOrPackage(pkg), pkg)
            }
    }

    // ---- Notification listeners --------------------------------------------

    private fun checkNotificationListeners(): List<AppInfo> {
        val raw = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return emptyList()

        return raw.split(":")
            .filter { it.isNotBlank() }
            .mapNotNull { flat ->
                try {
                    val pkg = ComponentName.unflattenFromString(flat)?.packageName ?: return@mapNotNull null
                    if (pkg == context.packageName) return@mapNotNull null
                    if (isSystemPackage(pkg)) null
                    else AppInfo(appLabelOrPackage(pkg), pkg)
                } catch (_: Exception) {
                    null
                }
            }
            .distinctBy { it.packageName }
    }

    // ---- Device admin apps ------------------------------------------------

    private fun checkDeviceAdmins(): List<AppInfo> {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            ?: return emptyList()
        return (dpm.activeAdmins ?: emptyList())
            .mapNotNull { component ->
                val pkg = component.packageName
                if (isSystemPackage(pkg)) null
                else AppInfo(appLabelOrPackage(pkg), pkg)
            }
    }

    // ---- Helpers ----------------------------------------------------------

    private fun isSystemPackage(packageName: String): Boolean {
        return try {
            val flags = context.packageManager
                .getApplicationInfo(packageName, 0).flags
            (flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun appLabelOrPackage(packageName: String): String {
        return try {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            packageName
        }
    }
}
