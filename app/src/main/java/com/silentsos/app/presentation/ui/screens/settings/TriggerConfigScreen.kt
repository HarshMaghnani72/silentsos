package com.silentsos.app.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.presentation.viewmodel.SettingsViewModel
import com.silentsos.app.ui.components.GlassCard
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.components.SilentToggleSwitch
import com.silentsos.app.ui.theme.*

@Composable
fun TriggerConfigScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSecretPin by remember { mutableStateOf(false) }
    var showDuressPin by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background.copy(alpha = 0.8f))
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                    Text(
                        "SECURE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        ),
                        color = OnSurface
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Primary)
                }
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // ── Hero ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Activation Methods",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = OnSurface
                )
                Text(
                    "Define the precise triggers that initiate your silent emergency protocols. These methods are designed to be undetectable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // ── Power Button Pattern ──
            TriggerCard(
                icon = Icons.Default.PowerSettingsNew,
                title = "Power Button Pattern",
                subtitle = "Activate without looking at your screen.",
                isEnabled = uiState.triggerConfig.powerButtonEnabled,
                onToggle = { viewModel.updatePowerButtonEnabled(it) }
            ) {
                // Info box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                    Text(
                        "3 rapid presses of the side power button will trigger a silent SOS. The phone will vibrate once to confirm.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = OnSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            // ── Shake Sensitivity ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHigh)
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Secondary)
                        }
                        Column {
                            Text("Shake Sensitivity",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text("Vigorously shake your device to alert contacts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Slider(
                            value = uiState.triggerConfig.shakeSensitivity / 100f,
                            onValueChange = { viewModel.updateShakeSensitivity((it * 100).toInt()) },
                            colors = SliderDefaults.colors(
                                thumbColor = Secondary,
                                activeTrackColor = Secondary,
                                inactiveTrackColor = SurfaceContainerHighest
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LOW", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("MEDIUM", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("HIGH", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            // ── PIN Grid (Bento) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Secret PIN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Secret PIN",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text("Unlock the true interface from the calculator decoy.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = OnSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        OutlinedTextField(
                            value = uiState.triggerConfig.secretPin,
                            onValueChange = { viewModel.updateSecretPin(it) },
                            visualTransformation = if (showSecretPin) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp,
                                color = Secondary
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showSecretPin = !showSecretPin }) {
                                    Icon(
                                        if (showSecretPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = OutlineVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Secondary.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = SurfaceContainerLowest,
                                unfocusedContainerColor = SurfaceContainerLowest
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Duress PIN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(TertiaryContainer.copy(alpha = 0.1f))
                        .border(1.dp, TertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Duress PIN",
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                                    color = OnTertiaryContainer
                                )
                                Text(
                                    "CRITICAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = OnTertiaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TertiaryContainer.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text("Triggers silent alarm while showing a \"Success\" decoy screen.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = OnSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                        OutlinedTextField(
                            value = uiState.triggerConfig.duressPin,
                            onValueChange = { viewModel.updateDuressPin(it) },
                            visualTransformation = if (showDuressPin) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp,
                                color = OnTertiaryContainer
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showDuressPin = !showDuressPin }) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = OutlineVariant, modifier = Modifier.size(18.dp))
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Tertiary.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = SurfaceContainerLowest,
                                unfocusedContainerColor = SurfaceContainerLowest
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // ── Protocol Status ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(4.dp, Secondary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Secondary, modifier = Modifier.size(28.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Protocols Synchronized",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text("Last secure backup: 4 minutes ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainerHigh,
                            contentColor = OnSurface
                        )
                    ) {
                        Text(
                            "TEST CONNECTION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Secondary)
                    }
                    Column {
                        Text(title,
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
                SilentToggleSwitch(checked = isEnabled, onCheckedChange = onToggle)
            }
            extraContent()
        }
    }
}
