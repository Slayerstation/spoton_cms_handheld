package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoton.cms.db.Expense
import com.spoton.cms.navigation.components.BookkeepingComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookkeepingScreen(component: BookkeepingComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boekhouding & BI", fontWeight = FontWeight.Bold, color = SpotOnOrange) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug", tint = SpotOnOrange)
                    }
                },
                actions = {
                    IconButton(onClick = component::exportToCSV) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = SpotOnOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { component.setShowAddExpenseDialog(true) },
                containerColor = SpotOnOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Voeg uitgave toe")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpotOnOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.summary?.let { summary ->
                    item {
                        Text("Overzicht", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MetricCard(
                                title = "Bruto Omzet",
                                amount = summary.grossRevenue,
                                icon = Icons.Default.TrendingUp,
                                color = Color.Green,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Netto Winst",
                                amount = summary.netProfit,
                                icon = Icons.Default.AccountBalanceWallet,
                                color = SpotOnOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MetricCard(
                                title = "BTW Afdracht",
                                amount = summary.totalVat,
                                icon = Icons.Default.AccountBalance,
                                color = Color.Yellow,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Kosten (COGS+Fees)",
                                amount = summary.costOfGoodsSold + summary.mollieFees + summary.myParcelCosts,
                                icon = Icons.Default.TrendingDown,
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Handmatige Uitgaven", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (state.expenses.isEmpty()) {
                    item {
                        Text("Geen uitgaven geregistreerd.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(state.expenses) { expense ->
                        ExpenseItem(expense = expense, onDelete = { component.deleteExpense(expense.id) })
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Fab padding
                }
            }
        }

        if (state.showAddExpenseDialog) {
            AddExpenseDialog(component)
        }

        if (state.message != null) {
            AlertDialog(
                onDismissRequest = component::clearMessage,
                title = { Text("Melding", color = SpotOnOrange) },
                text = { Text(state.message!!, style = MaterialTheme.typography.bodySmall) },
                confirmButton = {
                    TextButton(onClick = component::clearMessage) {
                        Text("OK", color = SpotOnOrange)
                    }
                }
            )
        }
    }
}

@Composable
private fun MetricCard(title: String, amount: Double, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassColors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
            }
            Text("€${amount.format(2)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    val date = Instant.fromEpochMilliseconds(expense.date).toLocalDateTime(TimeZone.currentSystemDefault())
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(expense.description ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("${date.dayOfMonth}-${date.monthNumber}-${date.year}", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
            }
            Text("-€${expense.amount.format(2)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Red)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Verwijder", tint = Color.Gray)
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(component: BookkeepingComponent) {
    val state by component.state.collectAsState()
    val categories = listOf("Algemeen", "Ingrediënten", "Verpakking", "Brandstof", "Marketing", "Software")

    AlertDialog(
        onDismissRequest = { component.setShowAddExpenseDialog(false) },
        title = { Text("Nieuwe Uitgave", color = SpotOnOrange) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.newExpenseAmount,
                    onValueChange = component::updateNewExpenseAmount,
                    label = { Text("Bedrag (€)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Simple Dropdown for Category
                Text("Categorie", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = state.newExpenseCategory == cat,
                            onClick = { component.updateNewExpenseCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SpotOnOrange, selectedLabelColor = Color.White)
                        )
                    }
                }

                OutlinedTextField(
                    value = state.newExpenseDescription,
                    onValueChange = component::updateNewExpenseDescription,
                    label = { Text("Omschrijving (optioneel)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SpotOnOrange),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    if (state.newExpenseReceiptUri != null) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SpotOnOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Bon bijgevoegd", color = Color.White)
                    } else {
                        TextButton(onClick = component::pickReceipt) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = SpotOnOrange)
                            Spacer(Modifier.width(8.dp))
                            Text("Selecteer Bon", color = SpotOnOrange)
                        }
                        TextButton(onClick = component::takeReceiptPhoto) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SpotOnOrange)
                            Spacer(Modifier.width(8.dp))
                            Text("Maak Foto", color = SpotOnOrange)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = component::saveExpense, colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)) {
                Text("Opslaan")
            }
        },
        dismissButton = {
            TextButton(onClick = { component.setShowAddExpenseDialog(false) }) {
                Text("Annuleer", color = Color.Gray)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
