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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.domain.model.SOSEvent
import com.silentsos.app.domain.model.SOSStatus
import com.silentsos.app.domain.model.TriggerType
import com.silentsos.app.presentation.viewmodel.HistoryViewModel
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.components.StatusPill
import com.silentsos.app.ui.components.PillType
import com.silentsos.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncidentHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }

    // Filter events based on search query
    val filteredEvents = remember(uiState.events, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) uiState.events
        else uiState.events.filter { event ->
            event.triggerType.name.contains(uiState.searchQuery, ignoreCase = true) ||
            event.status.name.contains(uiState.searchQuery, ignoreCase = true) ||
            dateFormat.format(Date(event.startedAt)).contains(uiState.searchQuery, ignoreCase = true)
        }
    }

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

            // ── Loading State ──
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Secondary, strokeWidth = 2.dp)
                }
            }
            // ── Empty State ──
            else if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = OnSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (uiState.searchQuery.isNotBlank()) "No matching events" else "No incidents recorded",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (uiState.searchQuery.isNotBlank()) "Try adjusting your search" else "Your safety record is clean",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            // ── Events List ──
            else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredEvents.forEach { event ->
                        val (icon, iconTint, title) = getEventVisuals(event)
                        val pillText = event.status.name
                        val hasLocalAudio = event.id in uiState.eventsWithLocalAudio
                        val pillType = when (event.status) {
                            SOSStatus.ACTIVE -> PillType.SUCCESS
                            SOSStatus.CANCELLED -> PillType.NEUTRAL
                            SOSStatus.RESOLVED -> PillType.SUCCESS
                            SOSStatus.PENDING -> PillType.SYSTEM
                            SOSStatus.ESCALATED -> PillType.CRITICAL
                        }
                        val subtitle = buildString {
                            append(dateFormat.format(Date(event.startedAt)))
                            if (event.id.length >= 5) {
                                append(" • ID: SOS-${event.id.takeLast(5).uppercase()}")
                            }
                        }

                        HistoryListItem(
                            icon = icon,
                            iconTint = iconTint,
                            title = title,
                            subtitle = subtitle,
                            detail = if (hasLocalAudio) "Audio saved on this device" else null,
                            pillText = pillText,
                            pillType = pillType
                        )
                    }
                }
            }
        }
    }
}

/** Maps event trigger type and status to visual icons and labels. */
private fun getEventVisuals(event: SOSEvent): Triple<androidx.compose.ui.graphics.vector.ImageVector, Color, String> {
    return when (event.triggerType) {
        TriggerType.MANUAL -> Triple(
            Icons.Default.Warning,
            OnTertiaryContainer,
            "SOS Activated"
        )
        TriggerType.POWER_BUTTON -> Triple(
            Icons.Default.PowerSettingsNew,
            OnTertiaryContainer,
            "Power Button SOS"
        )
        TriggerType.SHAKE -> Triple(
            Icons.Default.Vibration,
            Secondary,
            "Shake SOS Triggered"
        )
        TriggerType.DURESS_PIN -> Triple(
            Icons.Default.Warning,
            Error,
            "Duress Alert"
        )
        TriggerType.SECRET_CODE -> Triple(
            Icons.Default.Lock,
            Secondary,
            "Secret Code Trigger"
        )
    }
}

@Composable
private fun HistoryListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    detail: String?,
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnSurface
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    if (detail != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            detail,
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary
                        )
                    }
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
