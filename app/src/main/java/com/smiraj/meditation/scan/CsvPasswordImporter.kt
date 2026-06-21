package com.smiraj.meditation.scan

import android.content.Context
import android.net.Uri

/**
 * Parses password-manager CSV exports and enriches each entry with:
 *   - offline password-strength assessment
 *   - security-settings URL derived from known site map
 *
 * Compatible with Chrome, Bitwarden, 1Password, and KeePass CSV exports.
 * Nothing is sent over the network. File access is read-once; content is not
 * cached to disk.
 */
object CsvPasswordImporter {

    // ---- Known security-settings pages ------------------------------------

    private val SECURITY_URLS: Map<String, String> = mapOf(
        "google.com"           to "https://myaccount.google.com/security",
        "gmail.com"            to "https://myaccount.google.com/security",
        "mail.google.com"      to "https://myaccount.google.com/security",
        "myaccount.google.com" to "https://myaccount.google.com/security",
        "instagram.com"        to "https://www.instagram.com/accounts/login_activity/",
        "facebook.com"         to "https://www.facebook.com/settings?tab=security",
        "fb.com"               to "https://www.facebook.com/settings?tab=security",
        "twitter.com"          to "https://twitter.com/settings/security",
        "x.com"                to "https://twitter.com/settings/security",
        "microsoft.com"        to "https://account.microsoft.com/security",
        "live.com"             to "https://account.microsoft.com/security",
        "hotmail.com"          to "https://account.microsoft.com/security",
        "outlook.com"          to "https://account.microsoft.com/security",
        "apple.com"            to "https://appleid.apple.com/",
        "icloud.com"           to "https://appleid.apple.com/",
        "amazon.com"           to "https://www.amazon.com/ap/cnep",
        "viber.com"            to "https://account.viber.com/en/",
        "account.viber.com"    to "https://account.viber.com/en/",
        "whatsapp.com"         to "https://www.whatsapp.com/",
        "telegram.org"         to "https://my.telegram.org/auth",
        "tiktok.com"           to "https://www.tiktok.com/setting/",
        "snapchat.com"         to "https://accounts.snapchat.com/accounts/",
        "linkedin.com"         to "https://www.linkedin.com/psettings/",
        "yahoo.com"            to "https://login.yahoo.com/account/security",
        "netflix.com"          to "https://www.netflix.com/YourAccount",
        "paypal.com"           to "https://www.paypal.com/myaccount/security",
        "github.com"           to "https://github.com/settings/security",
        "gitlab.com"           to "https://gitlab.com/-/profile/account",
        "dropbox.com"          to "https://www.dropbox.com/account/security",
        "spotify.com"          to "https://www.spotify.com/account/overview/",
        "twitch.tv"            to "https://www.twitch.tv/settings/security",
    )

    // ---- Public API --------------------------------------------------------

    /**
     * Load and parse the bundled demo CSV from assets.
     * Returns an empty list (not a crash) if the asset is missing.
     */
    fun demo(context: Context): List<CsvPasswordEntry> {
        return try {
            val text = context.assets.open("demo_passwords.csv").bufferedReader().readText()
            parseContent(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Read and parse a user-selected CSV file from a content URI.
     * Call on an IO dispatcher; the result should be delivered on Main.
     */
    fun fromUri(context: Context, uri: Uri): List<CsvPasswordEntry> {
        return try {
            val text = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.readText()
                ?: return emptyList()
            parseContent(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Parse CSV text. Accepts with or without a header row.
     * Expected columns: name, url, username, password (in that order).
     * Any line that doesn't have 4+ columns is skipped.
     */
    fun parseContent(csv: String): List<CsvPasswordEntry> {
        val lines = csv.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val dataLines = if (lines.firstOrNull()
                ?.lowercase()
                ?.startsWith("name") == true
        ) lines.drop(1) else lines

        return dataLines.mapNotNull { line ->
            val cols = splitCsvLine(line)
            if (cols.size < 4) return@mapNotNull null
            val (name, url, username, password) = cols
            if (name.isBlank() && url.isBlank()) return@mapNotNull null
            CsvPasswordEntry(
                name = name.trim(),
                url = url.trim(),
                username = username.trim(),
                password = password.trim(),
                passwordStrength = assessStrength(password.trim()),
                securityUrl = securityUrlFor(url.trim()),
            )
        }
    }

    // ---- Helpers -----------------------------------------------------------

    /** Minimal RFC-4180 CSV line splitter (handles double-quoted fields). */
    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val buf = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> { result += buf.toString(); buf.clear() }
                else -> buf.append(ch)
            }
        }
        result += buf.toString()
        return result
    }

    /**
     * Assess password strength.
     *
     * Weak   < 8 chars, or only one character class
     * Strong >= 12 chars with upper + lower + digit + special
     * Medium everything else
     */
    fun assessStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.Weak
        val hasUpper   = password.any { it.isUpperCase() }
        val hasLower   = password.any { it.isLowerCase() }
        val hasDigit   = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val classCount = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }
        return when {
            classCount <= 1 || password.length < 10 -> PasswordStrength.Weak
            password.length >= 12 && classCount >= 3 -> PasswordStrength.Strong
            else -> PasswordStrength.Medium
        }
    }

    /**
     * Extract the host from a URL string, stripping "www." prefix.
     */
    fun extractDomain(url: String): String {
        return try {
            android.net.Uri.parse(url).host?.removePrefix("www.") ?: url
        } catch (_: Exception) {
            url
        }
    }

    /**
     * Look up the security-settings page for a site URL.
     * Checks the full host first, then the registered domain (last two labels).
     */
    fun securityUrlFor(siteUrl: String): String {
        val host = extractDomain(siteUrl)
        SECURITY_URLS[host]?.let { return it }
        val parts = host.split(".")
        if (parts.size >= 2) {
            val registered = parts.takeLast(2).joinToString(".")
            SECURITY_URLS[registered]?.let { return it }
        }
        return siteUrl
    }
}
