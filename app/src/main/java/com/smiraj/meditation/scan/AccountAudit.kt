package com.smiraj.meditation.scan

import java.security.SecureRandom

/**
 * Builds [AccountEntry] lists for the Leči report.
 *
 * Two paths:
 *  1. [fromCsvEntries] — real path: convert parsed CSV rows into report entries,
 *     detecting password weakness, username reuse, and linking security pages.
 *  2. [demoAccounts] — baseline: static demo data shown immediately on entry.
 *     CSV import merges additional entries on top.
 *
 * All data stays in memory. Cleared on exitToCover() via LeciReport.demo().
 */
object AccountAudit {

    // ---- CSV → report entries ---------------------------------------------

    /**
     * Convert a list of parsed CSV rows into [AccountEntry] objects ready for
     * display in the Leči report.
     *
     * Risk signals detected:
     *  - Weak or Medium password strength
     *  - Same username/email used across multiple accounts (reuse)
     */
    fun fromCsvEntries(entries: List<CsvPasswordEntry>): List<AccountEntry> {
        val usernameCount = entries
            .groupBy { it.username.lowercase().trim() }
            .filterValues { it.size > 1 }
            .keys

        return entries.map { csv ->
            val risks = mutableListOf<String>()
            val usernameReused = csv.username.lowercase().trim() in usernameCount

            when (csv.passwordStrength) {
                PasswordStrength.Weak   -> risks += "Slaba lozinka — preporučuje se promena"
                PasswordStrength.Medium -> risks += "Lozinka je prihvatljiva, ali je moguće ojačati"
                PasswordStrength.Strong -> { /* no risk */ }
            }
            if (usernameReused) {
                risks += "Isti korisnik/email korišćen na više naloga"
            }

            val severity = when {
                csv.passwordStrength == PasswordStrength.Weak || usernameReused -> FindingSeverity.High
                csv.passwordStrength == PasswordStrength.Medium                 -> FindingSeverity.Medium
                else                                                             -> FindingSeverity.Low
            }

            val suggested = if (csv.passwordStrength != PasswordStrength.Strong) generatePassword() else null

            AccountEntry(
                label = csv.name,
                siteUrl = csv.url,
                securityUrl = csv.securityUrl,
                username = csv.username,
                riskReasons = risks,
                severity = severity,
                suggestedPassword = suggested,
            )
        }.sortedByDescending { it.severity.ordinal }
    }

    // ---- Baseline demo accounts -------------------------------------------

    /**
     * Static fake accounts shown immediately when the user enters the Safety screen.
     * These represent common account risks without requiring a CSV import.
     * CSV import merges additional entries on top.
     */
    fun demoAccounts(): List<AccountEntry> = listOf(
        AccountEntry(
            label = "Google nalog",
            siteUrl = "https://myaccount.google.com",
            securityUrl = "https://myaccount.google.com/security",
            username = "neske@gmail.com",
            riskReasons = listOf(
                "Aktivna sesija sa nepoznate lokacije (Novi Sad, pre 3 dana)",
                "Rezervna e-mail adresa nije proverena",
                "Dvofaktorska autentifikacija nije uključena",
            ),
            severity = FindingSeverity.High,
            suggestedPassword = generatePassword(),
        ),
        AccountEntry(
            label = "Instagram",
            siteUrl = "https://www.instagram.com",
            securityUrl = "https://www.instagram.com/accounts/login_activity/",
            username = "neske_user",
            riskReasons = listOf(
                "3 aktivne sesije na nepoznatim uređajima",
                "Slaba lozinka — preporučuje se promena",
            ),
            severity = FindingSeverity.High,
            suggestedPassword = generatePassword(),
        ),
        AccountEntry(
            label = "Facebook",
            siteUrl = "https://facebook.com",
            securityUrl = "https://www.facebook.com/settings?tab=security",
            username = "neske@gmail.com",
            riskReasons = listOf(
                "Porodično deljenje lokacije je aktivno",
                "Isti korisnik/email korišćen na više naloga",
            ),
            severity = FindingSeverity.High,
            suggestedPassword = generatePassword(),
        ),
        AccountEntry(
            label = "Viber",
            siteUrl = "https://account.viber.com",
            securityUrl = "https://account.viber.com/en/",
            username = "+381601234567",
            riskReasons = listOf(
                "Pozivi i poruke dostupni na uparenom desktop uređaju",
            ),
            severity = FindingSeverity.Medium,
            suggestedPassword = null,
        ),
    )

    // ---- Password generator -----------------------------------------------

    /**
     * Offline secure-random password generator.
     * 16 characters, no ambiguous chars (0/O, 1/l/I).
     * Uses SecureRandom — never leaves the device.
     */
    fun generatePassword(length: Int = 16): String {
        val alphabet = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789!@#%"
        val rng = SecureRandom()
        return (1..length)
            .map { alphabet[rng.nextInt(alphabet.length)] }
            .joinToString("")
    }
}
