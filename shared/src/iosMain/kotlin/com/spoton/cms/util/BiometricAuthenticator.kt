package com.spoton.cms.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun authenticate(title: String, subtitle: String): Boolean {
        // Placeholder for iOS
        return true
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    return remember { IosBiometricAuthenticator() }
}
