package com.spoton.cms.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoton.cms.navigation.components.LoginComponent
import com.spoton.cms.ui.theme.GlassColors
import com.spoton.cms.ui.theme.SpotOnOrange
import org.jetbrains.compose.resources.painterResource
import spotoncms.shared.generated.resources.Res
import spotoncms.shared.generated.resources.spoton_logo

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A0F00),
                        Color(0xFF0A0A0A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-200).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SpotOnOrange.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo / Brand
            Image(
                painter = painterResource(Res.drawable.spoton_logo),
                contentDescription = "SpotOn Logo",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "CMS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Light
                ),
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Glass card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassColors.cardBackground)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Login Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign in to your store",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Secure JWT Authentication Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50) // Success Green
                    )
                }

                // Username
                OutlinedTextField(
                    value = state.username,
                    onValueChange = component::onUsernameChanged,
                    label = { Text("Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotOnOrange,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = SpotOnOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password
                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = state.password,
                    onValueChange = component::onPasswordChanged,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { component.onLoginClicked() }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotOnOrange,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = SpotOnOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login button
                Button(
                    onClick = component::onLoginClicked,
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotOnOrange,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Advanced Settings (Collapsible)
                var showAdvanced by remember { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { showAdvanced = !showAdvanced },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (showAdvanced) "Hide Advanced" else "Advanced Settings",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }

                    AnimatedVisibility(visible = showAdvanced) {
                        OutlinedTextField(
                            value = state.serverUrl,
                            onValueChange = component::onServerUrlChanged,
                            label = { Text("Backend URL") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SpotOnOrange.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Powered by WordPress & WooCommerce",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
