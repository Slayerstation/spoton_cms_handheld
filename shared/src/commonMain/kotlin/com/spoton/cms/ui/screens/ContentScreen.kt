package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.spoton.cms.domain.model.ContentField
import com.spoton.cms.domain.model.ContentGroup
import com.spoton.cms.navigation.components.ContentComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(component: ContentComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Content Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.selectedGroup != null) {
                ContentGroupEditorScreen(component, state.selectedGroup!!)
            } else {
                CuratedGroupList(component, state.curatedGroups)
            }

            state.message?.let { msg ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(onClick = component::dismissMessage) { Text("OK") }
                    }
                ) { Text(msg) }
            }
        }
    }
}

@Composable
private fun CuratedGroupList(component: ContentComponent, groups: List<ContentGroup>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Curated Sections", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(groups) { group ->
            ContentGroupCard(group, onClick = { component.selectGroup(group) })
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Explorer", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text("Discover all other ACF fields (Coming Soon)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
private fun ContentGroupCard(group: ContentGroup, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = GlassColors.cardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(SpotOnOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = SpotOnOrange)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(group.label, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${group.fields.size} Fields", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
private fun ContentGroupEditorScreen(component: ContentComponent, group: ContentGroup) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = component::clearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(group.label, style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
            Button(
                onClick = component::pushLive,
                colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
            ) {
                Text("Push Live")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(group.fields) { field ->
                FieldEditor(
                    field = field, 
                    onValueChange = { newValue ->
                        component.updateField(field.key, newValue)
                    },
                    onPickImage = {
                        component.pickImage(field.key)
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun FieldEditor(
    field: ContentField, 
    onValueChange: (String) -> Unit,
    onPickImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(field.label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        
        when (field) {
            is ContentField.Text -> {
                OutlinedTextField(
                    value = field.value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = !field.isMultiline,
                    minLines = if (field.isMultiline) 3 else 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SpotOnOrange
                    )
                )
            }
            is ContentField.Toggle -> {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.DarkGray.copy(alpha = 0.3f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (field.value) "Enabled" else "Disabled", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = field.value,
                        onCheckedChange = { onValueChange(it.toString()) },
                        colors = SwitchDefaults.colors(checkedThumbColor = SpotOnOrange)
                    )
                }
            }
            is ContentField.Color -> {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.DarkGray.copy(alpha = 0.3f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color(android.graphics.Color.parseColor(field.hex))))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(field.hex, color = Color.White, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Palette, contentDescription = null, tint = Color.Gray)
                }
            }
            is ContentField.Image -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (field.attachmentId != null) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = SpotOnOrange)
                            Text("Image ID: ${field.attachmentId}", color = Color.White)
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("No image selected", color = Color.Gray)
                        }
                        TextButton(onClick = onPickImage) {
                            Text(if (field.attachmentId != null) "Change Photo" else "Select / Take Photo", color = SpotOnOrange)
                        }
                    }
                }
            }
            is ContentField.Repeater -> {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.DarkGray.copy(alpha = 0.2f)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    field.items.forEachIndexed { index, itemFields ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Item #${index + 1}", style = MaterialTheme.typography.labelSmall, color = SpotOnOrange, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { /* TODO: Remove item */ }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                                itemFields.forEach { (subKey, subField) ->
                                    FieldEditor(
                                        field = subField,
                                        onValueChange = { /* TODO: Update subfield */ },
                                        onPickImage = { /* TODO: Pick subfield image */ }
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { /* TODO: Add item */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add Item")
                    }
                }
            }
        }
    }
}
