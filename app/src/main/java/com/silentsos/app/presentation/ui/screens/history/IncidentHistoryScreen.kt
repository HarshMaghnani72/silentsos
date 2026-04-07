package com.silentsos.app.presentation.ui.screens.history

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.presentation.viewmodel.HistoryViewModel
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.components.StatusPill
import com.silentsos.app.ui.components.PillType
import com.silentsos.app.ui.theme.*

@Composable
fun IncidentHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Header ──
            Text(
                "Incident Log",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Encrypted archive of all safety activations and system tests.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Search Bar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search events or dates...", color = Outline) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Outline) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OutlineVariant.copy(alpha = 0.1f),
                        unfocusedBorderColor = OutlineVariant.copy(alpha = 0.1f),
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow
                    )
                )
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerHigh,
                        contentColor = OnSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filter", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Featured Event Card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(TertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = OnTertiaryContainer)
                            }
                            Column {
                                Text("SOS Activated",
                                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text("Aug 12, 10:45 PM • ID: SOS-88219",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(9999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = OnSecondary),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Report", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Map placeholder + details bento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Map
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(SurfaceContainerHigh, SurfaceDim)
                                    )
                                ),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            // Fake map grid
                            Icon(Icons.Default.Map, contentDescription = null, tint = OnSurface.copy(alpha = 0.05f),
                                modifier = Modifier.size(120.dp).align(Alignment.Center))

                            // Coordinate overlay
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(SurfaceContainerHighest.copy(alpha = 0.8f))
                                    .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(9999.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                                Text(
                                    "LAT: 41.8781, LON: -87.6298",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 10.sp
                                    ),
                                    color = OnSurface
                                )
                            }
                        }

                        // Side details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Audio buffer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainerHigh)
                                    .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("AUDIO BUFFER", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Secondary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OnSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(SurfaceVariant)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.33f)
                                                        .fillMaxHeight()
                                                        .background(Secondary)
                                                )
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("0:12", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = OnSurfaceVariant)
                                                Text("0:45", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = OnSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }

                            // Contacts Alerted
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainerHigh)
                                    .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("CONTACTS ALERTED", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Row {
                                        repeat(3) { i ->
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .offset(x = (-12 * i).dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceContainerHighest)
                                                    .border(2.dp, SurfaceContainerHigh, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Text("Local dispatch + 3 guardians.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                        color = OnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Historical Events List ──
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HistoryListItem(
                    icon = Icons.Default.VerifiedUser,
                    iconTint = Secondary,
                    title = "Test Mode Activation",
                    subtitle = "Aug 10, 2:15 PM • System Diagnosis Clear",
                    pillText = "PASSED",
                    pillType = PillType.SUCCESS
                )
                HistoryListItem(
                    icon = Icons.Default.History,
                    iconTint = Outline,
                    title = "Geofence Alert (Auto-Log)",
                    subtitle = "Aug 08, 11:30 PM • Zone: Downtown South",
                    pillText = "RESOLVED",
                    pillType = PillType.NEUTRAL
                )
                HistoryListItem(
                    icon = Icons.Default.WifiTethering,
                    iconTint = Outline,
                    title = "Network Handshake Sync",
                    subtitle = "Aug 05, 09:00 AM • Routine integrity check",
                    pillText = "SYSTEM",
                    pillType = PillType.SYSTEM
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Load More ──
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.2f))
                )
            ) {
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Older Events",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HistoryListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    pillText: String,
    pillType: PillType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineVariant.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { }
            .padding(24.dp)
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
                        .background(SurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnSurface
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(text = pillText, type = pillType)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline.copy(alpha = 0.5f))
            }
        }
    }
}
