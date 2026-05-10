package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.spoton.cms.domain.model.OrderStatus
import com.spoton.cms.navigation.components.OrderDetailComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(component: OrderDetailComponent) {
    val state by component.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order #${state.order?.number ?: ""}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    state.order?.number?.let { number ->
                        IconButton(onClick = { 
                            clipboardManager.setText(AnnotatedString(number))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Order Number", tint = SpotOnOrange)
                        }
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

        state.order?.let { order ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassColors.cardBackground)
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val statusColor = when (order.status) {
                            OrderStatus.PROCESSING -> Color(0xFF2196F3)
                            OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                            OrderStatus.ON_HOLD -> Color(0xFFFFC107)
                            else -> SpotOnOrange
                        }
                        Box(Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                        Text(
                            order.status.name.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = statusColor
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        order.billing?.fullName ?: "Customer",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        order.billing?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Logistics Card (MyParcel Integration)
                if (!order.isB2B && order.billingWeightKg > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFF1E88E5).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocalShipping, "Shipping", tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                            Text("Logistics Metrics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Billed Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("${order.billingWeightKg.format(2)} kg", fontWeight = FontWeight.Bold)
                            }
                            order.bestRate?.let { rate ->
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Suggested Carrier", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text("${rate.carrierName} (€${rate.price.format(2)})", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                        
                        Button(
                            onClick = { /* Generate Label Placeholder */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate MyParcel Label")
                        }
                    }
                }

                // B2B Wholesale Card
                if (order.isB2B) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF673AB7).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFF673AB7).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Business, "Wholesale", tint = Color(0xFFBB86FC), modifier = Modifier.size(20.dp))
                            Text("Wholesale Invoice", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        
                        OrderInfoRow("Payment Terms", order.paymentTerms)
                        OrderInfoRow("Due Date", order.dueDate)
                        
                        if (order.status == OrderStatus.PENDING) {
                            Button(
                                onClick = { component.settleInvoice() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isUpdating
                            ) {
                                Text("Settle Invoice & Mark Paid")
                            }
                        }
                    }
                }

                // Line Items Card
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassColors.cardBackground)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Order Items", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    
                    order.lineItems.forEach { item ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${item.quantity}x", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(item.sku, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                            Text("€${item.total}", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("${order.currencySymbol}${order.total}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpotOnOrange))
                    }
                }

                // Shipping Address Card
                order.shipping?.let { address ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(GlassColors.cardBackground)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Shipping Address", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(address.fullAddress, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                // Status Update
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassColors.cardBackground)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Order Status", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            OrderStatus.PENDING to "Pending",
                            OrderStatus.PROCESSING to "Processing",
                            OrderStatus.COMPLETED to "Completed"
                        ).forEach { (status, label) ->
                            val isSelected = order.status == status
                            FilterChip(
                                selected = isSelected,
                                onClick = { component.updateStatus(status) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpotOnOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Extension to format double to 2 decimal places in CommonMain
fun Double.format(digits: Int) = this.toString().let {
    val parts = it.split(".")
    if (parts.size == 1) "$it.00"
    else parts[0] + "." + parts[1].padEnd(digits, '0').take(digits)
}

@Composable
fun OrderInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}
