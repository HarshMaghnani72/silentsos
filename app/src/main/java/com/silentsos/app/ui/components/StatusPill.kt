package com.silentsos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silentsos.app.ui.theme.*

enum class PillType {
    SUCCESS, WARNING, NEUTRAL, CRITICAL, SYSTEM
}

@Composable
fun StatusPill(
    text: String,
    type: PillType = PillType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (type) {
        PillType.SUCCESS -> Secondary.copy(alpha = 0.1f) to Secondary
        PillType.WARNING -> TertiaryContainer.copy(alpha = 0.3f) to Tertiary
        PillType.CRITICAL -> TertiaryContainer.copy(alpha = 0.3f) to OnTertiaryContainer
        PillType.NEUTRAL -> SurfaceVariant to OnSurfaceVariant
        PillType.SYSTEM -> SurfaceVariant to OnSurfaceVariant
    }

    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        ),
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
