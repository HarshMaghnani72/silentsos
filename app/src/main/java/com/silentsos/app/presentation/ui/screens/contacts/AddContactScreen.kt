package com.silentsos.app.presentation.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.domain.model.ContactPriorityLevel
import com.silentsos.app.presentation.viewmodel.ContactsViewModel
import com.silentsos.app.ui.components.GlassCard
import com.silentsos.app.ui.components.SecureTopBar
import com.silentsos.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onBackClick: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.addSuccess) {
        if (uiState.addSuccess) {
            viewModel.clearAddSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            SecureTopBar(
                showBackButton = true,
                title = "Add Contact",
                onBackClick = onBackClick
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "New Guardian",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = OnSurface
            )
            Text(
                "Add a trusted individual to your safety network using their full international phone number.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            // Name Field
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.newName,
                    onValueChange = { viewModel.updateNewName(it) },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Secondary,
                        focusedLabelColor = Secondary
                    )
                )
            }

            // Phone Field
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.newPhone,
                    onValueChange = { viewModel.updateNewPhone(it) },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OnSurfaceVariant) },
                    placeholder = { Text("+1 (555) 000-0000", color = Outline) },
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Secondary,
                        focusedLabelColor = Secondary
                    )
                )
            }

            // Priority Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "PRIORITY LEVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PriorityOption(
                        label = "High",
                        isSelected = uiState.newPriority == ContactPriorityLevel.HIGH,
                        accentColor = OnTertiaryContainer,
                        bgColor = TertiaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateNewPriority(ContactPriorityLevel.HIGH) }
                    )
                    PriorityOption(
                        label = "Medium",
                        isSelected = uiState.newPriority == ContactPriorityLevel.MEDIUM,
                        accentColor = Primary,
                        bgColor = PrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateNewPriority(ContactPriorityLevel.MEDIUM) }
                    )
                    PriorityOption(
                        label = "Low",
                        isSelected = uiState.newPriority == ContactPriorityLevel.LOW,
                        accentColor = OnSurfaceVariant,
                        bgColor = SurfaceContainerHigh,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateNewPriority(ContactPriorityLevel.LOW) }
                    )
                }
            }

            // Error
            uiState.error?.let { error ->
                Text(
                    error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorContainer.copy(alpha = 0.2f))
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { viewModel.addContact() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(9999.dp),
                enabled = !uiState.isLoading && uiState.newName.isNotBlank() && uiState.newPhone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = OnSecondary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnSecondary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add to Safety Network",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PriorityOption(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) bgColor else SurfaceContainerLow)
            .then(
                if (isSelected) Modifier.border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                else Modifier.border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (isSelected) accentColor else OnSurfaceVariant
        )
    }
}
