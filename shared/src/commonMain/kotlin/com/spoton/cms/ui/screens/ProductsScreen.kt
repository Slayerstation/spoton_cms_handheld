package com.spoton.cms.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoton.cms.navigation.components.ProductsComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductsScreen(component: ProductsComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (state.isSelectionMode) {
                        Text("${state.selectedIds.size} Selected", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Products", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    if (state.isSelectionMode) {
                        IconButton(onClick = component::clearSelection) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                        }
                    } else {
                        IconButton(onClick = component.onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (state.isSelectionMode) {
                BulkActionBar(
                    onAdjustPrice = component::bulkUpdatePrices,
                    isProcessing = state.isProcessing
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar (hidden in selection mode)
            if (!state.isSelectionMode) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = component::onSearchQueryChanged,
                    placeholder = { Text("Search products...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SpotOnOrange) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotOnOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isLoading && !state.isSelectionMode) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SpotOnOrange)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.products) { product ->
                        val isSelected = state.selectedIds.contains(product.id)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) SpotOnOrange.copy(alpha = 0.1f) else GlassColors.cardBackground)
                                .combinedClickable(
                                    onClick = { 
                                        if (state.isSelectionMode) component.toggleSelection(product.id)
                                        else component.onProductSelected(product.id)
                                    },
                                    onLongClick = { component.toggleSelection(product.id) }
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { component.toggleSelection(product.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = SpotOnOrange)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (product.sku.isNotBlank()) {
                                    Text(
                                        text = "SKU: ${product.sku}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "€${product.price}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = SpotOnOrange
                                )
                                product.stockQuantity?.let { qty ->
                                    Text(
                                        text = "$qty in stock",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (qty <= 5) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BulkActionBar(
    onAdjustPrice: (Float) -> Unit,
    isProcessing: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BulkActionItem(
                icon = Icons.Default.TrendingUp,
                label = "+5%",
                onClick = { onAdjustPrice(5f) },
                enabled = !isProcessing
            )
            BulkActionItem(
                icon = Icons.Default.TrendingDown,
                label = "-5%",
                onClick = { onAdjustPrice(-5f) },
                enabled = !isProcessing
            )
            VerticalDivider(modifier = Modifier.height(24.dp))
            BulkActionItem(
                icon = Icons.Default.Category,
                label = "Category",
                onClick = { /* TODO: Show Category Picker */ },
                enabled = !isProcessing
            )
            BulkActionItem(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = { /* TODO */ },
                enabled = !isProcessing,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun BulkActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = if (enabled) color else color.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (enabled) color else color.copy(alpha = 0.3f))
    }
}
