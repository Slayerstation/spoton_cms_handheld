package com.spoton.cms.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Native barcode scanner view.
 * Android: Uses CameraX + ML Kit
 * iOS: Uses AVFoundation
 */
@Composable
expect fun BarcodeScannerView(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
)
