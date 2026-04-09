package com.silentsos.app.presentation.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.presentation.viewmodel.DashboardViewModel
import com.silentsos.app.ui.components.GlassCard
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenDashboardScreen(
    onNavigateToSetup: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSOSTriggered: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to Active SOS screen when countdown completes and SOS fired
    LaunchedEffect(uiState.isSosTriggering, uiState.sosCountdownSeconds) {
        if (!uiState.isSosTriggering && uiState.sosCountdownSeconds == 0 && uiState.sosError == null) {
            // Check if SOS was just triggered (contacts > 0 means it worked)
        }
    }

    // Pulse animation for status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ), label = "pulseAlpha"
    )

    Scaffold(
        topBar = { SecureTopBar(showNetworkIcon = true) },
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
            Spacer(modifier = Modifier.height(8.dp))

            // ── Status Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Security watermark
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = OnSurface.copy(alpha = 0.03f),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Pulsing green dot
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .scale(pulseScale + 0.5f)
                                .clip(CircleShape)
                                .background(Secondary.copy(alpha = pulseAlpha))
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Secondary)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "YOU ARE PROTECTED",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = OnSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "SYSTEM ACTIVE & ENCRYPTED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        ),
                        color = OnSurfaceVariant
                    )
                }
            }

            // ── SOS Trigger Bento Grid ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SOS Main Trigger — shows countdown when active
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (uiState.isSosTriggering) {
                                viewModel.cancelSOSCountdown()
                            } else {
                                viewModel.triggerSOS()
                            }
                        },
                    showBorder = true,
                    borderColor = if (uiState.isSosTriggering) Secondary.copy(alpha = 0.5f) else OutlineVariant.copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .border(
                                    2.dp,
                                    if (uiState.isSosTriggering) Secondary else OutlineVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSosTriggering) {
                                // Countdown number
                                Text(
                                    "${uiState.sosCountdownSeconds}",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = ManropeFontFamily,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = Secondary
                                )
                            } else {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Emergency",
                                    tint = OnSurface,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (uiState.isSosTriggering) "TAP TO CANCEL" else "TRIGGER SOS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (uiState.isSosTriggering) Secondary else OnSurface
                            )
                            Text(
                                if (uiState.isSosTriggering) "SOS WILL ACTIVATE" else "TAP TO START COUNTDOWN",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Status Cluster
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusRow(Icons.Default.LocationOn, "GPS: ${uiState.gpsStatus}", "PRECISE", Secondary)
                    StatusRow(Icons.Default.BatteryFull, "Battery: ${uiState.batteryLevel}%", "STABLE", Primary)
                    StatusRow(Icons.Default.SignalCellularAlt, "Network: ${uiState.networkStatus}", "OPTIMAL", Outline)
                }
            }

            // ── Trusted Contacts ──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Trusted Contacts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = ManropeFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = OnSurface
                        )
                        Text("Receiving silent alerts", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    TextButton(onClick = onNavigateToSetup) {
                        Text(
                            "MANAGE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = Secondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Stacked avatars
                        Row {
                            repeat(minOf(uiState.contacts.size, 3)) { i ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .offset(x = (-12 * i).dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh)
                                        .border(4.dp, SurfaceContainerLowest, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        uiState.contacts.getOrNull(i)?.name?.take(1)?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                        }
                        Column {
                            Text(
                                "${uiState.contacts.size} Contacts Linked",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = OnSurface
                            )
                            Text(
                                "REAL-TIME SYNC ENABLED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Secondary
                            )
                        }
                    }
                }
            }

            // ── Protocol Banner ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TertiaryContainer.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .border(1.dp, TertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(TertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = OnTertiaryContainer)
                    }
                    Column {
                        Text(
                            "Protocol 01 Active",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "If SOS is triggered, 5-second delay before high-encryption transmit to local authorities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, status: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .padding(start = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(48.dp)
                .background(accentColor)
                .align(Alignment.CenterStart)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
            }
            Text(
                status,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
    }
}
