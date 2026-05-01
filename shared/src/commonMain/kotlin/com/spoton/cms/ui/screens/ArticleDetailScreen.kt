package com.spoton.cms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import com.spoton.cms.navigation.components.ArticleDetailComponent
import com.spoton.cms.ui.components.editor.SpotOnEditor
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange
import com.spoton.cms.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(component: ArticleDetailComponent) {
    val state by component.state.collectAsState()
    val title by component.titleState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.article == null) "New Article" else "Edit Article", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component.onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = component::publish,
                        enabled = !state.isSaving && title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotOnOrange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Publish", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = component::saveDraftManual,
                        enabled = !state.isAutosaving,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpotOnOrange)
                    ) {
                        Text("Save Draft", fontWeight = FontWeight.SemiBold)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Autosave status
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (state.isAutosaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = SpotOnOrange.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Saving...",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpotOnOrange.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        state.lastSaved?.let { timestamp ->
                            Text(
                                text = "Draft saved at ${formatTimestamp(timestamp)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }

                // Title Input
                val titleFocusRequester = remember { FocusRequester() }
                
                LaunchedEffect(state.isLoading) {
                    if (!state.isLoading && title.isBlank()) {
                        titleFocusRequester.requestFocus()
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { component.titleState.value = it },
                    placeholder = { Text("Article Title", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotOnOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = GlassColors.cardBackground,
                        unfocusedContainerColor = GlassColors.cardBackground
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                // Content Editor
                val customTextSelectionColors = TextSelectionColors(
                    handleColor = SpotOnOrange,
                    backgroundColor = SpotOnOrange.copy(alpha = 0.2f)
                )

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    SpotOnEditor(
                        state = component.contentState,
                        minHeight = 400,
                        modifier = Modifier.fillMaxWidth(),
                        title = title
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
