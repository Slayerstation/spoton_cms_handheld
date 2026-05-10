package com.spoton.cms.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoton.cms.navigation.components.DashboardComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange
import org.jetbrains.compose.resources.painterResource
import spotoncms.shared.generated.resources.Res
import spotoncms.shared.generated.resources.spoton_logo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(component: DashboardComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.spoton_logo),
                            contentDescription = "SpotOn Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SpotOn CMS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Dashboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = component.onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Pending",
                        value = state.pendingOrderCount.toString(),
                        icon = Icons.Default.Notifications,
                        color = SpotOnOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Products",
                        value = state.totalProducts.toString(),
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // System Health row
            state.systemInfo?.let { system ->
                item {
                    SystemHealthCard(system)
                }
            }

            // Quick actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Products",
                        subtitle = "Manage stock, prices & descriptions",
                        icon = Icons.Default.ShoppingCart,
                        color = SpotOnOrange,
                        onClick = component.onNavigateToProducts
                    )
                    QuickActionCard(
                        title = "Articles",
                        subtitle = "Write blog posts with offline autosave",
                        icon = Icons.Default.Edit,
                        color = Color(0xFFFF9800),
                        onClick = component.onNavigateToArticles
                    )
                    QuickActionCard(
                        title = "Orders",
                        subtitle = "Track and manage customer orders",
                        icon = Icons.AutoMirrored.Filled.List,
                        color = Color(0xFF2196F3),
                        onClick = component.onNavigateToOrders
                    )
                    QuickActionCard(
                        title = "Inventory",
                        subtitle = "Quick stock updates with barcode scanner",
                        icon = Icons.Default.Search,
                        color = Color(0xFF4CAF50),
                        onClick = component.onNavigateToInventory
                    )
                    QuickActionCard(
                        title = "Styles",
                        subtitle = "Customize webshop colors & appearance",
                        icon = Icons.Default.Palette,
                        color = Color(0xFF9C27B0),
                        onClick = component.onNavigateToStyles
                    )
                    QuickActionCard(
                        title = "Settings",
                        subtitle = "Universal shop, legal & integration config",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF607D8B),
                        onClick = component.onNavigateToSettings
                    )
                    QuickActionCard(
                        title = "Chat",
                        subtitle = "Unified Inbox for WhatsApp, IG & Email",
                        icon = Icons.Default.Email,
                        color = Color(0xFFE91E63),
                        onClick = component.onNavigateToChat
                    )
                    QuickActionCard(
                        title = "Content (ACF)",
                        subtitle = "Manage website text, banners & USPs",
                        icon = Icons.Default.Edit,
                        color = Color(0xFF00BCD4),
                        onClick = component.onNavigateToContent
                    )
                    QuickActionCard(
                        title = "Bookkeeping",
                        subtitle = "Track revenue, VAT, fees & margins",
                        icon = Icons.Default.AccountBalance,
                        color = Color(0xFFFF5722), // Deep Orange
                        onClick = component.onNavigateToBookkeeping
                    )
                }
            }

            // Recent orders
            if (state.recentOrders.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Orders",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.recentOrders) { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassColors.cardBackground)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "#${order.number}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = order.billing?.fullName ?: "Unknown",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${order.currencySymbol}${order.total}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SpotOnOrange
                            )
                            Text(
                                text = order.status.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SpotOnOrange)
            }
        }
    }
}

@Composable
private fun SystemHealthCard(system: com.spoton.cms.domain.model.BackendSystemInfo) {
    val hosting = system.hosting
    val isOnline = hosting.status == "connected"
    val healthColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColors.cardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(healthColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Infrastructure Health",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = hosting.provider,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SSL Status
            HealthIndicator(
                label = "SSL Status",
                value = if (hosting.ssl?.active == true) "Secure" else "Inactive",
                icon = Icons.Default.CheckCircle,
                color = if (hosting.ssl?.active == true) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            
            // Disk Usage (if available)
            hosting.usage?.disk?.let { disk ->
                HealthIndicator(
                    label = "Disk Usage",
                    value = "${disk.percent?.toInt() ?: 0}%",
                    icon = Icons.Default.Info,
                    color = when {
                        (disk.percent ?: 0.0) > 90.0 -> Color(0xFFF44336)
                        (disk.percent ?: 0.0) > 70.0 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HealthIndicator(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        color.copy(alpha = 0.05f)
                    )
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColors.cardBackground)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
