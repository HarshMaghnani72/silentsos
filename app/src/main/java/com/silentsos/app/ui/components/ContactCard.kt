package com.silentsos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silentsos.app.ui.theme.*

enum class ContactPriority {
    HIGH, MEDIUM, LOW
}

@Composable
fun ContactCard(
    name: String,
    phone: String,
    priority: ContactPriority,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {}
) {
    val (priorityLabel, priorityBg, priorityTextColor, priorityBorderColor) = when (priority) {
        ContactPriority.HIGH -> listOf(
            "High Priority",
            TertiaryContainer.copy(alpha = 0.3f),
            Tertiary,
            TertiaryContainer.copy(alpha = 0.5f)
        )
        ContactPriority.MEDIUM -> listOf(
            "Medium",
            PrimaryContainer,
            Primary,
            OutlineVariant.copy(alpha = 0.2f)
        )
        ContactPriority.LOW -> listOf(
            "Low",
            SurfaceContainerHigh,
            OnSurfaceVariant,
            OutlineVariant.copy(alpha = 0.1f)
        )
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    if (priority == ContactPriority.HIGH) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(TertiaryContainer)
                                .border(2.dp, Background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = OnTertiaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = OnSurface
                    )
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = OnSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = (priorityLabel as String).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    ),
                    color = priorityTextColor as Color,
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(priorityBg as Color)
                        .border(1.dp, priorityBorderColor as Color, RoundedCornerShape(9999.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = OnSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
