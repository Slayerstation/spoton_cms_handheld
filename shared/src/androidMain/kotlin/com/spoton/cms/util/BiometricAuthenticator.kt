package com.spoton.cms.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidBiometricAuthenticator(
    private val activity: FragmentActivity
) : BiometricAuthenticator {
    override suspend fun authenticate(title: String, subtitle: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (continuation.isActive) continuation.resume(false)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Keep waiting for success or explicit cancel
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }
}

@Composable
actual fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    return remember(context) {
        AndroidBiometricAuthenticator(context as FragmentActivity)
    }
}
