package com.silentsos.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SilentSOSShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),      // DEFAULT from Stitch: 1rem
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),   // lg from Stitch: 2rem
)

// Additional shape constants used across the app
object AppShapes {
    val BottomNavRadius = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val CardRadius = RoundedCornerShape(16.dp)
    val PillRadius = RoundedCornerShape(9999.dp)
    val ButtonRadius = RoundedCornerShape(12.dp)
    val ChipRadius = RoundedCornerShape(8.dp)
}
