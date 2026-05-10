package com.spoton.cms.util

import androidx.compose.runtime.Composable

/**
 * Handles biometric / device passphrase authentication.
 */
interface BiometricAuthenticator {
    suspend fun authenticate(
        title: String = "Beveiligde Toegang",
        subtitle: String = "Verifieer met uw apparaatwachtwoord of biometrie om de API-sleutels te bekijken."
    ): Boolean
}

@Composable
expect fun rememberBiometricAuthenticator(): BiometricAuthenticator
