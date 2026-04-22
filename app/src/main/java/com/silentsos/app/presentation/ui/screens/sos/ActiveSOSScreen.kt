package com.silentsos.app.presentation.ui.screens.sos

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.presentation.viewmodel.SOSViewModel
import com.silentsos.app.ui.components.GlassCard
import com.silentsos.app.ui.theme.*

@Composable
fun ActiveSOSScreen(
    onEndProtocol: () -> Unit,
    viewModel: SOSViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStopDialog by remember { mutableStateOf(false) }
    var securePin by remember { mutableStateOf("") }

    LaunchedEffect(uiState.resolutionCompleted) {
        if (uiState.resolutionCompleted) {
            viewModel.consumeResolution()
            onEndProtocol()
        }
    }

    // Pulse animation for SOS indicator
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    val barAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bar"
    )
    val statusLabel = uiState.activeEvent?.status?.name ?: "ACTIVE"
    val notifiedContactsLabel = if (uiState.notifiedContacts.isEmpty()) {
        "Notifications are still being delivered"
    } else {
        "Notified: ${uiState.notifiedContacts.joinToString()}"
    }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Status Bar Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                // Active badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Secondary.copy(alpha = 0.1f))
                        .border(1.dp, Secondary.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Secondary)
                    )
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = Secondary
                    )
                }
            }

            // ── Main Status Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Pulsing SOS ring
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(1.4f)
                                .clip(CircleShape)
                                .background(Secondary.copy(alpha = pulseAlpha * 0.15f))
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(3.dp, Secondary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "SOS Active",
                                tint = Secondary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "PROTOCOL ACTIVE",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Elapsed time
                    val minutes = uiState.elapsedSeconds / 60
                    val seconds = uiState.elapsedSeconds % 60
                    Text(
                        "Active for ${String.format("%02d:%02d", minutes, seconds)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        notifiedContactsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            // ── Active Operations Grid ──
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "ACTIVE OPERATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )

                OperationRow(
                    icon = Icons.Default.LocationOn,
                    label = "GPS Transmitting",
                    detail = "LAT: ${String.format("%.4f", uiState.currentLatitude)} LON: ${String.format("%.4f", uiState.currentLongitude)}",
                    accentColor = Secondary,
                    isActive = uiState.isTransmittingLocation
                )
                OperationRow(
                    icon = Icons.Default.Mic,
                    label = "Audio Recording",
                    detail = "High-fidelity ambient capture",
                    accentColor = Secondary,
                    isActive = uiState.isRecordingAudio
                )
                OperationRow(
                    icon = Icons.Default.Shield,
                    label = "Evidence Archive",
                    detail = "Encrypted upload in progress",
                    accentColor = Secondary,
                    isActive = uiState.isCapturingEvidence
                )
            }

            if (uiState.notifiedContacts.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CONTACTS NOTIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            uiState.notifiedContacts.joinToString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface
                        )
                    }
                }
            }

            uiState.cancelError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(TertiaryContainer.copy(alpha = 0.18f))
                        .border(1.dp, TertiaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnTertiaryContainer
                    )
                }
            }

            // ── Network Signal Bars ──
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "NETWORK INTEGRITY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            "OPTIMAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Signal bars visualization
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        repeat(24) { i ->
                            val height = (12 + (i * barAnim * 1.5f).toInt() % 28)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(height.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (i < 18) Secondary.copy(alpha = 0.6f + (i * 0.02f))
                                        else SurfaceContainerHighest
                                    )
                            )
                        }
                    }
                }
            }

            // ── End Protocol Button ──
            Button(
                onClick = { showStopDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TertiaryContainer,
                    contentColor = OnTertiaryContainer
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "END PROTOCOL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = {
                showStopDialog = false
                securePin = ""
                viewModel.clearCancelError()
            },
            title = { Text("Resolve Active SOS") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter your secure PIN to stop location sharing, stop recording, and mark this SOS as resolved.")
                    OutlinedTextField(
                        value = securePin,
                        onValueChange = { securePin = it.filter(Char::isDigit).take(12) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Secure PIN") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelSOS(securePin)
                        securePin = ""
                    },
                    enabled = !uiState.isCancelling
                ) {
                    Text(if (uiState.isCancelling) "Stopping..." else "Resolve SOS")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        securePin = ""
                        viewModel.clearCancelError()
                    }
                ) {
                    Text("Keep Active")
                }
            }
        )
    }
}

@Composable
private fun OperationRow(
    icon: ImageVector,
    label: String,
    detail: String,
    accentColor: Color,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .then(
                if (isActive) Modifier.border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(20.dp)
    ) {
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        color = OnSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) accentColor else Outline)
            )
        }
    }
}
