package com.smiraj.meditation.safety

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Biometric anchor: verifies the device owner before entering the secret layer.
 *
 * This prevents an abuser from exploring the diagnostics screen if they
 * physically access the unlocked phone — the secret trigger alone is not enough.
 *
 * If no biometric or device credential is enrolled, entry is allowed without a prompt
 * so the feature degrades gracefully on unprotected devices.
 *
 * Neutral UI strings — no DV/SOS language visible during the prompt.
 */
object BiometricGate {

    private val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * Shows a biometric/PIN prompt anchored to [activity].
     * [onSuccess] is called on the main thread if auth succeeds or biometrics are unavailable.
     * [onCancel] is called if the user explicitly dismisses the prompt.
     */
    fun prompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onCancel: () -> Unit,
    ) {
        val manager = BiometricManager.from(activity)
        when (manager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> { /* show prompt below */ }
            else -> {
                // Biometrics not enrolled or hardware missing — allow entry without auth
                onSuccess()
                return
            }
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> onCancel()
                        // Hardware/timeout errors: allow entry to avoid locking out the user
                        else -> onSuccess()
                    }
                }

                override fun onAuthenticationFailed() {
                    // Wrong fingerprint/face — prompt stays open, do nothing
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verifikacija")
            .setSubtitle("Nastavi sa pregledom")
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
