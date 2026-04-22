package com.silentsos.app.presentation.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.presentation.viewmodel.AuthViewModel
import com.silentsos.app.ui.theme.*

@Composable
fun PhoneAuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo/Icon
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Primary
            )

            // Title
            Text(
                text = "Verify Your Phone",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = OnSurface
            )

            Text(
                text = if (uiState.codeSent) {
                    "Enter the 6-digit code sent to\n${uiState.phoneNumber}"
                } else {
                    "Enter your phone number in international format to continue"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.codeSent) {
                // Phone Number Input
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::updatePhoneNumber,
                    label = { Text("Phone Number") },
                    placeholder = { Text("+15551234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
                    )
                )

                Button(
                    onClick = {
                        activity?.let { viewModel.sendVerificationCode(it) }
                            ?: run {
                                // Show error if activity is null
                                viewModel.clearError()
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && uiState.phoneNumber.startsWith("+") && uiState.phoneNumber.length >= 8 && activity != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = OnPrimary
                        )
                    } else {
                        Text("Send Code", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // OTP Input
                OutlinedTextField(
                    value = uiState.otpCode,
                    onValueChange = viewModel::updateOtpCode,
                    label = { Text("Verification Code") },
                    placeholder = { Text("123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OnSurfaceVariant.copy(alpha = 0.3f)
                    )
                )

                Button(
                    onClick = viewModel::verifyCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && uiState.otpCode.length == 6,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = OnPrimary
                        )
                    } else {
                        Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(
                    onClick = {
                        activity?.let { viewModel.resendCode(it) }
                    },
                    enabled = !uiState.isLoading && activity != null
                ) {
                    Text("Resend Code", color = Primary)
                }
            }

            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (uiState.statusMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.statusMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = OnSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
