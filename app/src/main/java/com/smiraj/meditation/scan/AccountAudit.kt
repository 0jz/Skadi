package com.smiraj.meditation.scan

import java.security.SecureRandom

/**
 * Account audit data for the Leči report.
 *
 * MVP uses offline demo data to illustrate the kinds of risks the user should
 * manually check on their accounts.
 *
 * Production path:
 *  - User selects a CSV via Android file picker
 *  - Parsed locally, held in Android Keystore-encrypted working copy
 *  - Plaintext only briefly in memory
 *  - No cloud upload
 *  - User changes passwords manually on provider sites
 *
 * All data is in memory only. Cleared on exitToCover().
 */
object AccountAudit {

    /**
     * Demo accounts with realistic (but entirely fake) risk signals.
     * Each entry includes an offline-generated suggested password where
     * a password change is advisable.
     */
    fun demoAccounts(): List<AccountEntry> = listOf(
        AccountEntry(
            label = "Google nalog",
            siteUrl = "https://myaccount.google.com",
            securityUrl = "https://myaccount.google.com/security",
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
            riskReasons = listOf(
                "3 aktivne sesije na nepoznatim uređajima",
                "Slaba lozinka (manje od 10 znakova)",
            ),
            severity = FindingSeverity.High,
            suggestedPassword = generatePassword(),
        ),
        AccountEntry(
            label = "Facebook",
            siteUrl = "https://facebook.com",
            securityUrl = "https://www.facebook.com/settings?tab=security",
            riskReasons = listOf(
                "Porodično deljenje lokacije je aktivno",
                "2 sesije sa nepoznatih uređaja",
            ),
            severity = FindingSeverity.High,
            suggestedPassword = generatePassword(),
        ),
        AccountEntry(
            label = "Gmail (posao)",
            siteUrl = "https://mail.google.com",
            securityUrl = "https://myaccount.google.com/security",
            riskReasons = listOf(
                "Automatsko prosleđivanje e-pošte je aktivno — proveri primaoca",
            ),
            severity = FindingSeverity.Medium,
            suggestedPassword = generatePassword(),
        ),
        AccountEntry(
            label = "Viber",
            siteUrl = "https://account.viber.com",
            securityUrl = "https://account.viber.com/en/",
            riskReasons = listOf(
                "Pozivi i poruke dostupni na uparenom desktop uređaju",
            ),
            severity = FindingSeverity.Medium,
            suggestedPassword = null,
        ),
    )

    /**
     * Offline secure-random password generator.
     *
     * 16 characters from a safe printable alphabet (no ambiguous chars like
     * 0/O, 1/l/I). Uses SecureRandom — never sent over the network.
     */
    fun generatePassword(length: Int = 16): String {
        val alphabet = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789!@#%"
        val rng = SecureRandom()
        return (1..length)
            .map { alphabet[rng.nextInt(alphabet.length)] }
            .joinToString("")
    }
}
