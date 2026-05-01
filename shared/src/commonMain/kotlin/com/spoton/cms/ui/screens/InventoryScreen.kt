package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoton.cms.navigation.components.InventoryComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(component: InventoryComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = component::toggleScanner) {
                        Icon(Icons.Default.Search, contentDescription = "Scan Barcode", tint = SpotOnOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            // Stats Bar (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InventoryStatCard("Total Value", "€${state.totalValue.toInt()}", SpotOnOrange)
                InventoryStatCard("Low Stock", "${state.lowStockCount}", Color(0xFFFFA726))
                InventoryStatCard("Out of Stock", "${state.outOfStockCount}", Color(0xFFEF5350))
                InventoryStatCard("Items", "${state.products.size}", Color.White.copy(alpha = 0.6f))
            }

            // Category Filter (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.categories.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { component.onCategorySelected(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpotOnOrange,
                            selectedLabelColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = state.selectedCategory == category,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Scanner placeholder banner
            if (state.isScannerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                ) {
                    com.spoton.cms.ui.components.BarcodeScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeScanned = { barcode ->
                            component.onBarcodeScanned(barcode)
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Scanned product card
            state.scannedProduct?.let { product ->
                var stockInput by remember(product.id) { mutableStateOf(product.stockQuantity?.toString() ?: "0") }

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassColors.cardBackground)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Scanned Product", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(product.name, style = MaterialTheme.typography.bodyLarge)
                    Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                    OutlinedTextField(
                        value = stockInput, onValueChange = { stockInput = it },
                        label = { Text("New Stock") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { stockInput.toIntOrNull()?.let { component.updateStock(product.id, it) } },
                            enabled = !state.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Update") }
                        OutlinedButton(
                            onClick = component::clearScannedProduct,
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Dismiss") }
                    }

                    if (state.saveSuccess) {
                        Text("✓ Stock updated!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Product list
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = SpotOnOrange) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.filteredProducts) { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassColors.cardBackground)
                                .clickable { component.onProductClicked(product.id) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("SKU: ${product.sku.ifBlank { "—" }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    if (product.series.isNotBlank()) {
                                        Surface(
                                            color = SpotOnOrange.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = product.series.uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                                                color = SpotOnOrange,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${product.stockQuantity ?: "—"}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if ((product.stockQuantity ?: 0) <= product.threshold) MaterialTheme.colorScheme.error else SpotOnOrange
                                )
                                Text(
                                    text = product.unit.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryStatCard(label: String, value: String, color: Color) {
    Surface(
        color = GlassColors.cardBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}
