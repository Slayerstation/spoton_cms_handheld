package com.spoton.cms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun BarcodeScannerView(
    modifier: Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    // Note: iOS implementation requires AVFoundation integration.
    // For now, we provide a placeholder that compiles.
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("iOS Scanner (AVFoundation)", color = Color.White)
    }
}
