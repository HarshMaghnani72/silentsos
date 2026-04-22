package com.silentsos.app.presentation.ui.screens.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.domain.model.AutoDeletePeriod
import com.silentsos.app.domain.model.DisguiseType
import com.silentsos.app.presentation.viewmodel.SettingsViewModel
import com.silentsos.app.ui.components.GlassCard
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.components.SilentToggleSwitch
import com.silentsos.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Primary
                    )
                }
                Text(
                    "SilentSOS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = OnSurface
                )
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
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "PROFILE") {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Primary)
                        }
                        Column {
                            Text(
                                uiState.profileDisplayName.ifBlank { "Protected user" },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = OnSurface
                            )
                            Text(
                                uiState.profilePhoneNumber.ifBlank { "Phone profile sync will appear here" },
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ══ Section: Stealth Triggers ══
            SettingsSection(title = "STEALTH TRIGGERS") {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // Power Button Pattern
                    SettingsToggleRow(
                        title = "Power Button Pattern",
                        subtitle = "3-press default",
                        checked = uiState.triggerConfig.powerButtonEnabled,
                        onCheckedChange = { viewModel.updatePowerButtonEnabled(it) }
                    )
                    Divider(color = OutlineVariant.copy(alpha = 0.1f))

                    // Shake Sensitivity
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Shake Sensitivity",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = ManropeFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = OnSurface
                            )
                            Text(
                                "CALIBRATED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Secondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Secondary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = uiState.triggerConfig.shakeSensitivity / 100f,
                            onValueChange = { viewModel.updateShakeSensitivity((it * 100).toInt()) },
                            colors = SliderDefaults.colors(
                                thumbColor = Secondary,
                                activeTrackColor = Secondary,
                                inactiveTrackColor = SurfaceContainerHigh
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LOW", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("HIGH", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }
                    Divider(color = OutlineVariant.copy(alpha = 0.1f))

                    // Voice Activation
                    SettingsToggleRow(
                        title = "Voice Activation",
                        subtitleContent = {
                            Text(
                                "SETUP PHRASE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Secondary
                            )
                        },
                        checked = uiState.triggerConfig.voiceActivationEnabled,
                        onCheckedChange = { viewModel.updateVoiceActivation(it) }
                    )
                }
            }

            // ══ Section: Active Disguise ══
            SettingsSection(title = "ACTIVE DISGUISE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DisguiseOption(
                        icon = Icons.Default.Calculate,
                        label = "Calculator",
                        subtitle = "Selected",
                        isSelected = uiState.activeDisguise == DisguiseType.CALCULATOR,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateActiveDisguise(DisguiseType.CALCULATOR) }
                    )
                    DisguiseOption(
                        icon = Icons.Default.Description,
                        label = "Notes",
                        subtitle = "Utility",
                        isSelected = uiState.activeDisguise == DisguiseType.NOTES,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateActiveDisguise(DisguiseType.NOTES) }
                    )
                    DisguiseOption(
                        icon = Icons.Default.Checklist,
                        label = "To-do List",
                        subtitle = "Task Mgmt",
                        isSelected = uiState.activeDisguise == DisguiseType.TODO_LIST,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateActiveDisguise(DisguiseType.TODO_LIST) }
                    )
                }
            }

            // ══ Section: Privacy & Data ══
            SettingsSection(title = "PRIVACY & DATA") {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // Auto-delete
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(Icons.Default.AutoDelete, contentDescription = null, tint = Primary)
                            Text("Auto-delete recordings",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLowest)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("24h" to AutoDeletePeriod.TWENTY_FOUR_HOURS, "7d" to AutoDeletePeriod.SEVEN_DAYS, "Never" to AutoDeletePeriod.NEVER).forEach { (label, period) ->
                                val isActive = uiState.autoDeletePeriod == period
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(if (isActive) Modifier.background(SurfaceContainerHigh) else Modifier)
                                        .clickable { viewModel.updateAutoDeletePeriod(period) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isActive) OnSurface else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Divider(color = OutlineVariant.copy(alpha = 0.05f))

                    // Location Sharing
                    SettingsToggleRow(
                        title = "Location Sharing",
                        icon = Icons.Default.LocationOn,
                        checked = uiState.isLocationSharingEnabled,
                        onCheckedChange = { viewModel.updateLocationSharing(it) }
                    )
                    Divider(color = OutlineVariant.copy(alpha = 0.05f))

                    // Battery Optimization
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.BatterySaver, contentDescription = null, tint = Primary)
                            Text("Battery Optimization",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                    }
                }
            }

            // ══ Danger Zone ══
            OutlinedButton(
                onClick = { viewModel.wipeAllData() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TertiaryContainer)
                )
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = OnTertiaryContainer, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "WIPE ALL PROTOCOL DATA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = OnTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    subtitleContent: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Primary)
            }
            Column {
                Text(title,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                subtitleContent?.invoke()
            }
        }
        SilentToggleSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DisguiseOption(
    icon: ImageVector,
    label: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        borderColor = if (isSelected) Secondary.copy(alpha = 0.5f) else OutlineVariant.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(16.dp).align(Alignment.End)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Secondary.copy(alpha = 0.1f) else SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isSelected) Secondary else OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                Text(subtitle.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = OnSurfaceVariant)
            }
        }
    }
}
