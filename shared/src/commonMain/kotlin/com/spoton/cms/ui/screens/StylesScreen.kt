package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoton.cms.domain.model.StyleConfig
import com.spoton.cms.navigation.components.StylesComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylesScreen(component: StylesComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Styles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpotOnOrange)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Light mode colors
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassColors.cardBackground)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Light Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                ColorEditRow("Primary", state.config.primary) { color ->
                    component.updateConfig(state.config.copy(primary = color))
                }
                ColorEditRow("Primary Text", state.config.primaryForeground) { color ->
                    component.updateConfig(state.config.copy(primaryForeground = color))
                }
                ColorEditRow("Background", state.config.background) { color ->
                    component.updateConfig(state.config.copy(background = color))
                }
                ColorEditRow("Text Color", state.config.foreground) { color ->
                    component.updateConfig(state.config.copy(foreground = color))
                }
            }

            // Dark mode colors
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassColors.cardBackground)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                ColorEditRow("Background", state.config.darkBackground) { color ->
                    component.updateConfig(state.config.copy(darkBackground = color))
                }
                ColorEditRow("Text Color", state.config.darkForeground) { color ->
                    component.updateConfig(state.config.copy(darkForeground = color))
                }
            }

            // Save button
            Button(
                onClick = component::saveStyles,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save & Push to Webshop", fontWeight = FontWeight.Bold)
                }
            }

            if (state.saveSuccess) {
                Text("✓ Styles pushed to production", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            state.error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorEditRow(label: String, hexValue: String, onColorChanged: (String) -> Unit) {
    var inputValue by remember(hexValue) { mutableStateOf(hexValue) }
    val parsedColor = com.spoton.cms.ui.util.parseHexColor(hexValue)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Color preview circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(parsedColor)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            OutlinedTextField(
                value = inputValue,
                onValueChange = {
                    inputValue = it
                    if (it.startsWith("#") && (it.length == 7 || it.length == 9)) {
                        onColorChanged(it)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
