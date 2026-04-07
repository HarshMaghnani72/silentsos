package com.silentsos.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silentsos.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureTopBar(
    modifier: Modifier = Modifier,
    showNetworkIcon: Boolean = false,
    showBackButton: Boolean = false,
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background.copy(alpha = 0.8f),
            titleContentColor = OnSurface
        ),
        navigationIcon = {
            if (showBackButton && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Back",
                        tint = Primary
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = Secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title ?: "SECURE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            fontSize = 18.sp
                        ),
                        color = OnSurface
                    )
                }
            }
        },
        title = {
            if (showBackButton && title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Primary
                )
            }
        },
        actions = {
            if (showNetworkIcon) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = "Network",
                        tint = Primary
                    )
                }
            }
            IconButton(onClick = { onMenuClick?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Primary
                )
            }
        }
    )
}
