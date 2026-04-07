package com.silentsos.app.presentation.ui.screens.decoy

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silentsos.app.ui.theme.*

/**
 * Fake "Everything's Fine" screen shown when duress PIN is entered.
 * While this decoy is displayed, a silent SOS has already been triggered in the background.
 */
@Composable
fun FakeFineScreen(
    onDismiss: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "check")
    val checkScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

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
            // Success check icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(Secondary.copy(alpha = 0.1f))
                    .border(2.dp, Secondary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "System Verified",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    "All security protocols are functioning normally. No threats detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Fake status rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FakeStatusRow(Icons.Default.Shield, "Firewall", "Active")
                FakeStatusRow(Icons.Default.Lock, "Encryption", "AES-256")
                FakeStatusRow(Icons.Default.WifiTethering, "Connection", "Secure")
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceContainerHigh,
                    contentColor = OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Return to App",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun FakeStatusRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = OnSurface)
        }
        Text(value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Secondary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Secondary.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
