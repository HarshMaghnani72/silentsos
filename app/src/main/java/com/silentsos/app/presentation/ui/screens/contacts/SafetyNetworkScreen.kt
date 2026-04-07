package com.silentsos.app.presentation.ui.screens.contacts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.domain.model.ContactPriorityLevel
import com.silentsos.app.presentation.viewmodel.ContactsViewModel
import com.silentsos.app.ui.components.ContactCard
import com.silentsos.app.ui.components.ContactPriority
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.theme.*

@Composable
fun SafetyNetworkScreen(
    onBackClick: () -> Unit,
    onAddContact: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SecureTopBar(
                showBackButton = true,
                title = "Safety Network",
                onBackClick = onBackClick
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Hero Section ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(SecondaryContainer.copy(alpha = 0.1f))
                    .border(1.dp, Secondary.copy(alpha = 0.2f), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Secondary)
                )
                Text(
                    "ACTIVE PROTECTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = Secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "The Guardian Circle",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    fontSize = 30.sp
                ),
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Manage the trusted individuals notified instantly when your safety status changes.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Contact Cards ──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                uiState.contacts.forEach { contact ->
                    val priority = when (contact.priority) {
                        ContactPriorityLevel.HIGH -> ContactPriority.HIGH
                        ContactPriorityLevel.MEDIUM -> ContactPriority.MEDIUM
                        ContactPriorityLevel.LOW -> ContactPriority.LOW
                    }
                    ContactCard(
                        name = contact.name,
                        phone = contact.phoneNumber,
                        priority = priority,
                        onEditClick = { /* Navigate to edit */ }
                    )
                }

                // Add New Contact Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = OutlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onAddContact() }
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = OnSurfaceVariant)
                        }
                        Text(
                            "Add New Contact",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Protocol Status Card ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Secondary.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        "PROTOCOL STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Encrypted connection active. Your contacts will be notified via secure satellite backup if cellular fails.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Sync Button ──
            Button(
                onClick = { /* Sync */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = OnSecondary
                )
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sync Safety Network",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }
    }
}
