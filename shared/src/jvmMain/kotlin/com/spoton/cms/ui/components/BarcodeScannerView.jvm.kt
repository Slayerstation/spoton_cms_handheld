package com.spoton.cms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoton.cms.ui.theme.SpotOnOrange

@Composable
actual fun BarcodeScannerView(
    modifier: Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    var manualSku by remember { mutableStateOf("") }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Desktop Scanner Simulator",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Webcam scanning is not yet implemented for Desktop.\nPlease enter a SKU manually to simulate a scan.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = manualSku,
                onValueChange = { manualSku = it },
                label = { Text("Enter SKU") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotOnOrange,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.width(200.dp)
            )

            Button(
                onClick = { if (manualSku.isNotBlank()) onBarcodeScanned(manualSku) },
                colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simulate Scan")
            }
        }
    }
}
