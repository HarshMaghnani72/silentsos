package com.silentsos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.silentsos.app.ui.theme.*

enum class BottomNavTab {
    DASHBOARD, EMERGENCY, HISTORY, SETTINGS
}

@Composable
fun SOSBottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(SurfaceContainer.copy(alpha = 0.6f))
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Outlined.GridView,
                selectedIcon = Icons.Filled.GridView,
                isSelected = selectedTab == BottomNavTab.DASHBOARD,
                onClick = { onTabSelected(BottomNavTab.DASHBOARD) }
            )
            BottomNavItem(
                icon = Icons.Outlined.GridView, // placeholder for emergency icon
                selectedIcon = Icons.Filled.GridView,
                isSelected = selectedTab == BottomNavTab.EMERGENCY,
                onClick = { onTabSelected(BottomNavTab.EMERGENCY) },
                useEmergencyIcon = true
            )
            BottomNavItem(
                icon = Icons.Outlined.History,
                selectedIcon = Icons.Filled.History,
                isSelected = selectedTab == BottomNavTab.HISTORY,
                onClick = { onTabSelected(BottomNavTab.HISTORY) }
            )
            BottomNavItem(
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings,
                isSelected = selectedTab == BottomNavTab.SETTINGS,
                onClick = { onTabSelected(BottomNavTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    useEmergencyIcon: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val activeColor = Secondary
    val inactiveColor = OnSurfaceVariant

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.background(SurfaceContainerHigh)
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
