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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoton.cms.domain.model.OrderStatus
import com.spoton.cms.navigation.components.OrdersComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(component: OrdersComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedFilter == null,
                    onClick = { component.onFilterChanged(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotOnOrange,
                        selectedLabelColor = Color.White
                    )
                )
                listOf(
                    OrderStatus.PROCESSING to "Processing",
                    OrderStatus.ON_HOLD to "On Hold",
                    OrderStatus.COMPLETED to "Completed",
                    OrderStatus.PENDING to "Pending"
                ).forEach { (status, label) ->
                    FilterChip(
                        selected = state.selectedFilter == status,
                        onClick = { component.onFilterChanged(status) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpotOnOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SpotOnOrange)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.orders) { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GlassColors.cardBackground)
                                .clickable { component.onOrderSelected(order.id) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "#${order.number}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        color = SpotOnOrange.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${order.totalItems} items",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpotOnOrange,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (order.isB2B) {
                                        Surface(
                                            color = Color(0xFF673AB7).copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "B2B",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF9575CD),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (order.billingWeightKg > 0) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${order.billingWeightKg}kg",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val city = order.billing?.city ?: ""
                                    Text(
                                        text = if (city.isNotBlank()) "${order.billing?.fullName} • $city" else (order.billing?.fullName ?: "Unknown"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    if (order.customerNote.isNotBlank()) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = "Has Note",
                                            tint = SpotOnOrange,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = order.dateCreated,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${order.currencySymbol}${order.total}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SpotOnOrange
                                )
                                val statusColor = when (order.status) {
                                    OrderStatus.PROCESSING -> Color(0xFF2196F3)
                                    OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                                    OrderStatus.ON_HOLD -> Color(0xFFFFC107)
                                    OrderStatus.CANCELLED -> Color(0xFFF44336)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = order.status.name.lowercase().replace("_", " ")
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor
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
