package com.spoton.cms.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class JvmBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun authenticate(title: String, subtitle: String): Boolean {
        // Placeholder for JVM
        return true
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    return remember { JvmBiometricAuthenticator() }
}
