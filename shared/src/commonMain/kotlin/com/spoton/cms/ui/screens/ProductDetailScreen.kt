package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spoton.cms.navigation.components.ProductDetailComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(component: ProductDetailComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.product?.name ?: "Product", fontWeight = FontWeight.Bold) },
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
        } else {
            state.product?.let { product ->
                var stockInput by remember(product.stockQuantity) { mutableStateOf(product.stockQuantity?.toString() ?: "0") }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product info card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassColors.cardBackground)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Product Info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        
                        var name by remember(product.name) { mutableStateOf(product.name) }
                        var sku by remember(product.sku) { mutableStateOf(product.sku) }
                        var price by remember(product.price) { mutableStateOf(product.price) }
                        var weight by remember(product.weight) { mutableStateOf(product.weight) }

                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Product Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = sku, onValueChange = { sku = it },
                                label = { Text("SKU") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weight, onValueChange = { weight = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = price, onValueChange = { price = it },
                            label = { Text("Regular Price (€)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (name != product.name || sku != product.sku || price != product.price || weight != product.weight) {
                            Button(
                                onClick = {
                                    component.updateProduct(mapOf(
                                        "name" to name,
                                        "sku" to sku,
                                        "regular_price" to price,
                                        "weight" to weight
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
                            ) {
                                Text("Save Product Info")
                            }
                        }
                    }

                    // Stock management card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassColors.cardBackground)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Stock Management", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                        OutlinedTextField(
                            value = stockInput,
                            onValueChange = { stockInput = it },
                            label = { Text("Stock Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                stockInput.toIntOrNull()?.let { qty ->
                                    component.updateStock(qty)
                                }
                            },
                            enabled = !state.isSaving,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Text("Update Stock", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (state.saveSuccess) {
                            Text("✓ Stock updated successfully", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Product Description Editor
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassColors.cardBackground)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Product Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                        com.spoton.cms.ui.components.editor.SpotOnEditor(
                            state = component.descriptionState,
                            minHeight = 250,
                            title = product.name
                        )

                        Button(
                            onClick = { component.saveDescription() },
                            enabled = !state.isSaving,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Text("Save Description", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Metadata & Assets
                    var showBarcode by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassColors.cardBackground)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Metadata & Assets", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            IconButton(onClick = { showBarcode = true }) {
                                Icon(Icons.Default.QrCode, contentDescription = "View Barcode", tint = SpotOnOrange)
                            }
                        }

                        DetailMetadataField("Series", product.series) { component.updateMetadata("_spoton_series", it) }
                        DetailMetadataField("Unit", product.unit) { component.updateMetadata("_spoton_unit", it) }
                        DetailMetadataField("Low Stock Threshold", product.threshold.toString()) { component.updateMetadata("_spoton_threshold", it) }
                    }

                    if (showBarcode) {
                        AlertDialog(
                            onDismissRequest = { showBarcode = false },
                            title = { Text("Product Asset Code", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                            // Mock barcode using placeholder API
                                            AsyncImage(
                                                model = "https://barcodeapi.org/api/128/${product.sku}?height=60&width=200",
                                                contentDescription = "Barcode",
                                                modifier = Modifier.height(60.dp).width(200.dp)
                                            )
                                            Text(product.sku, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                                        }
                                    }
                                    Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showBarcode = false }) { Text("Close") }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun DetailMetadataField(label: String, initialValue: String, onUpdate: (String) -> Unit) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            if (value != initialValue) {
                TextButton(onClick = { onUpdate(value) }) {
                    Text("Update", color = SpotOnOrange)
                }
            }
        }
    }
}
