package com.silentsos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.silentsos.app.ui.theme.*

@Composable
fun SilentToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f, label = "toggle"
    )

    Box(
        modifier = modifier
            .width(44.dp)
            .height(24.dp)
            .clip(CircleShape)
            .background(if (checked) Secondary else SurfaceContainerHighest)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .offset(x = (20 * thumbOffset).dp)
                .clip(CircleShape)
                .background(OnSurface)
                .align(Alignment.CenterStart)
        )
    }
}
