package com.silentsos.app.presentation.ui.screens.auth

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import com.silentsos.app.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    
    // Core foreground permissions
    val foregroundPermissions = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list
    }

    val multiplePermissionsState = rememberMultiplePermissionsState(foregroundPermissions)

    // Background location must be requested separately on Android 11+ (API 30+)
    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }

    val allForegroundGranted = multiplePermissionsState.allPermissionsGranted
    val backgroundGranted = backgroundLocationState == null || backgroundLocationState.status.isGranted

    // Automatically proceed if everything is granted
    LaunchedEffect(allForegroundGranted, backgroundGranted) {
        if (allForegroundGranted && backgroundGranted) {
            onPermissionsGranted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Shield",
                modifier = Modifier.size(64.dp),
                tint = Primary
            )

            Text(
                text = "Emergency Setup",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = OnSurface
            )

            Text(
                text = "To keep you safe, SilentSOS requires access to location, audio, and SMS triggers.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Status Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusRow("Core Services", allForegroundGranted)
                    if (backgroundLocationState != null) {
                        StatusRow("Background Safety", backgroundGranted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!allForegroundGranted) {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    } else if (backgroundLocationState != null && !backgroundLocationState.status.isGranted) {
                        backgroundLocationState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = when {
                        !allForegroundGranted -> "Grant Core Permissions"
                        !backgroundGranted -> "Enable Background Safety"
                        else -> "Continue"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (multiplePermissionsState.shouldShowRationale || (allForegroundGranted && !backgroundGranted)) {
                TextButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        "Permissions denied? Open System Settings",
                        color = Primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (allForegroundGranted && !backgroundGranted) {
                Text(
                    text = "Note: For background safety, please select 'Allow all the time' in the location settings page that opens.",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
        Text(
            text = if (isGranted) "Enabled" else "Required",
            color = if (isGranted) Color(0xFF4CAF50) else Primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
