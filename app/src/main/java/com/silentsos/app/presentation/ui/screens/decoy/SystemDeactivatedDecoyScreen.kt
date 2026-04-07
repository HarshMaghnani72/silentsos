package com.silentsos.app.presentation.ui.screens.decoy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silentsos.app.ui.theme.*

/**
 * Decoy screen that looks like the system is deactivated / disabled.
 * Shows a powered-off state to trick an adversary.
 */
@Composable
fun SystemDeactivatedDecoyScreen(
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh)
                    .border(2.dp, OutlineVariant.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = Outline,
                    modifier = Modifier.size(48.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "System Deactivated",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    "All monitoring protocols have been disabled. This device is no longer enrolled in any safety network.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Fake disabled status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DeactivatedRow("Monitoring", "Off")
                DeactivatedRow("Location", "Disabled")
                DeactivatedRow("Contacts", "Unlinked")
                DeactivatedRow("Encryption", "Cleared")
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9999.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.3f))
                )
            ) {
                Text(
                    "Uninstall App",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeactivatedRow(label: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(
            status,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Outline,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceContainerHighest)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
