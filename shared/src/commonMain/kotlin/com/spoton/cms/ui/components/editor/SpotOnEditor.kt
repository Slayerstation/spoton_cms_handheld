package com.spoton.cms.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.*
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotOnEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
    label: String? = null,
    minHeight: Int = 200,
    title: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassColors.cardBackground)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        SpotOnEditorToolbar(state = state)

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        Box(modifier = Modifier.fillMaxWidth()) {
            RichTextEditor(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight.dp),
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = { Text("Start typing your content...") }
            )
            
            // Stats & SEO Overlay (Reactive)
            val stats by remember(state.annotatedString, state.selection, title) {
                derivedStateOf {
                    val text = state.toText()
                    val words = if (text.isBlank()) 0 else text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    val chars = text.length
                    val selectionLength = if (state.selection.collapsed) 0 else state.selection.length
                    
                    // SEO Scoring
                    var score = 0
                    val checklist = mutableListOf<Pair<String, Boolean>>()
                    
                    // 1. Word Count (Target 300)
                    val wcMet = words >= 300
                    score += if (wcMet) 40 else (words * 40 / 300)
                    checklist.add("At least 300 words" to wcMet)
                    
                    // 2. Title Length (Target 40-70)
                    val tlMet = title.length in 40..70
                    score += if (tlMet) 20 else 0
                    checklist.add("Title length (40-70 chars)" to tlMet)
                    
                    // 3. Links
                    val hasLinks = state.isLink || state.toHtml().contains("href=")
                    score += if (hasLinks) 20 else 0
                    checklist.add("Include at least one link" to hasLinks)
                    
                    // 4. Structure (Headings)
                    val hasHeadings = state.toHtml().contains(Regex("<h[1-6]>"))
                    score += if (hasHeadings) 20 else 0
                    checklist.add("Use subheadings (H1-H6)" to hasHeadings)

                    object {
                        val wordCount = words
                        val charCount = chars
                        val selected = selectionLength
                        val time = (words / 200).coerceAtLeast(1)
                        val seoScore = score
                        val seoChecklist = checklist
                    }
                }
            }

            var showSeoDetails by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SEO Progress Bar
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        .clickable { showSeoDetails = true }
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "SEO Score: ${stats.seoScore}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            stats.seoScore < 40 -> Color(0xFFEF5350)
                            stats.seoScore < 80 -> SpotOnOrange
                            else -> Color(0xFF66BB6A)
                        },
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { stats.seoScore / 100f },
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        color = when {
                            stats.seoScore < 40 -> Color(0xFFEF5350)
                            stats.seoScore < 80 -> SpotOnOrange
                            else -> Color(0xFF66BB6A)
                        },
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }

                Text(
                    text = buildString {
                        append("${stats.wordCount} words | ${stats.time} min read")
                        if (stats.selected > 0) {
                            append(" (${stats.selected} selected)")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            if (showSeoDetails) {
                AlertDialog(
                    onDismissRequest = { showSeoDetails = false },
                    title = { Text("SEO Analysis", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stats.seoChecklist.forEach { (task, isDone) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isDone) Color(0xFF66BB6A) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = task,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSeoDetails = false }) {
                            Text("Got it")
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun SpotOnEditorToolbar(state: RichTextState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo / Redo (Temporarily disabled due to API changes in rc14)
        /*
        EditorActionIcon(
            icon = Icons.Default.Undo,
            isSelected = false,
            enabled = state.canUndo,
            onClick = { state.undo() }
        )
        EditorActionIcon(
            icon = Icons.Default.Redo,
            isSelected = false,
            enabled = state.canRedo,
            onClick = { state.redo() }
        )
        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
        */

        // Clear Formatting
        EditorActionIcon(
            icon = Icons.Default.FormatClear,
            isSelected = false,
            onClick = { state.removeSpanStyle(state.currentSpanStyle) }
        )

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

        EditorActionIcon(
            icon = Icons.Default.FormatBold,
            isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
            onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
        )
        EditorActionIcon(
            icon = Icons.Default.FormatItalic,
            isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
            onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
        )
        EditorActionIcon(
            icon = Icons.Default.FormatUnderlined,
            isSelected = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
            onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
        )
        EditorActionIcon(
            icon = Icons.Default.Code,
            isSelected = state.currentSpanStyle.fontFamily == androidx.compose.ui.text.font.FontFamily.Monospace,
            onClick = { 
                state.toggleSpanStyle(SpanStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    background = Color.LightGray.copy(alpha = 0.2f)
                )) 
            }
        )

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

        // Color Presets
        EditorColorAction(color = SpotOnOrange, onClick = { state.toggleSpanStyle(SpanStyle(color = SpotOnOrange)) })
        EditorColorAction(color = Color(0xFFEF5350), onClick = { state.toggleSpanStyle(SpanStyle(color = Color(0xFFEF5350))) }) // Red
        EditorColorAction(color = Color(0xFF78909C), onClick = { state.toggleSpanStyle(SpanStyle(color = Color(0xFF78909C))) }) // Blue Gray
        EditorActionIcon(
            icon = Icons.Default.FormatColorReset,
            isSelected = false,
            onClick = { state.toggleSpanStyle(SpanStyle(color = Color.Unspecified)) }
        )

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
        var showLinkDialog by remember { mutableStateOf(false) }
        var linkUrl by remember { mutableStateOf("") }
        var linkText by remember { mutableStateOf("") }
        
        EditorActionIcon(
            icon = Icons.Default.Link,
            isSelected = state.isLink,
            onClick = { 
                linkUrl = ""
                linkText = "" // Simplified: always ask for text for now
                showLinkDialog = true 
            }
        )

        if (showLinkDialog) {
            AlertDialog(
                onDismissRequest = { showLinkDialog = false },
                title = { Text("Insert Hyperlink") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = linkText,
                            onValueChange = { linkText = it },
                            label = { Text("Link Text") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = linkUrl,
                            onValueChange = { linkUrl = it },
                            label = { Text("URL (e.g., https://...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (linkUrl.isNotBlank()) {
                                state.addLink(
                                    text = if (linkText.isNotBlank()) linkText else linkUrl,
                                    url = if (linkUrl.startsWith("http")) linkUrl else "https://$linkUrl"
                                )
                            }
                            showLinkDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange)
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLinkDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

        EditorActionIcon(
            icon = Icons.Default.FormatListBulleted,
            isSelected = state.isUnorderedList,
            onClick = { state.toggleUnorderedList() }
        )
        EditorActionIcon(
            icon = Icons.Default.FormatListNumbered,
            isSelected = state.isOrderedList,
            onClick = { state.toggleOrderedList() }
        )

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

        // Alignment Dropdown
        var showAlignmentMenu by remember { mutableStateOf(false) }
        Box {
            EditorActionIcon(
                icon = Icons.Default.FormatAlignLeft,
                isSelected = false,
                onClick = { showAlignmentMenu = true }
            )
            DropdownMenu(
                expanded = showAlignmentMenu,
                onDismissRequest = { showAlignmentMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Left") },
                    leadingIcon = { Icon(Icons.Default.FormatAlignLeft, null) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left))
                        showAlignmentMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Center") },
                    leadingIcon = { Icon(Icons.Default.FormatAlignCenter, null) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
                        showAlignmentMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Right") },
                    leadingIcon = { Icon(Icons.Default.FormatAlignRight, null) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right))
                        showAlignmentMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Justify") },
                    leadingIcon = { Icon(Icons.Default.FormatAlignJustify, null) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Justify))
                        showAlignmentMenu = false
                    }
                )
            }
        }

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))

        // Headings Dropdown
        var showHeadingMenu by remember { mutableStateOf(false) }
        Box {
            EditorActionIcon(
                icon = Icons.Default.FormatSize,
                isSelected = false,
                onClick = { showHeadingMenu = true }
            )
            DropdownMenu(
                expanded = showHeadingMenu,
                onDismissRequest = { showHeadingMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Normal Text", fontWeight = FontWeight.Normal) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(lineHeight = 20.sp))
                        showHeadingMenu = false
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                DropdownMenuItem(
                    text = { Text("Heading 1", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(lineHeight = 40.sp))
                        showHeadingMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Heading 2", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(lineHeight = 32.sp))
                        showHeadingMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Heading 3", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    onClick = {
                        state.toggleParagraphStyle(ParagraphStyle(lineHeight = 26.sp))
                        showHeadingMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorActionIcon(
    icon: ImageVector,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SpotOnOrange.copy(alpha = 0.2f) else Color.Transparent)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                isSelected -> SpotOnOrange
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EditorTextAction(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SpotOnOrange.copy(alpha = 0.2f) else Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) SpotOnOrange else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EditorColorAction(
    color: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
    }
}
