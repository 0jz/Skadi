package com.smiraj.meditation.scan

/**
 * One row parsed from a password-manager CSV export.
 *
 * Standard export format (compatible with Chrome, Bitwarden, 1Password):
 *   name,url,username,password
 *
 * Sensitive fields are held in memory only and cleared on exitToCover().
 */
data class CsvPasswordEntry(
    val name: String,
    val url: String,
    val username: String,
    val password: String,
    val passwordStrength: PasswordStrength,
    /** Security-settings page for this site, derived from known site map. */
    val securityUrl: String,
)

enum class PasswordStrength {
    Weak,   // short, simple, or single character class
    Medium, // acceptable but improvable
    Strong, // long, mixed, special chars
}
