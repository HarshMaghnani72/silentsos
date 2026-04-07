package com.silentsos.app.presentation.ui.screens.disguise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentsos.app.presentation.viewmodel.CalculatorViewModel
import com.silentsos.app.ui.theme.*

@Composable
fun CalculatorScreen(
    onAccessDashboard: () -> Unit,
    onDuressTriggered: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            viewModel.resetAuth()
            onAccessDashboard()
        }
    }

    LaunchedEffect(uiState.isDuressTriggered) {
        if (uiState.isDuressTriggered) {
            viewModel.resetAuth()
            onDuressTriggered()
        }
    }

    Scaffold(
        topBar = {
            // Disguised top bar - looks like a secure calculator app
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background.copy(alpha = 0.8f))
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = OnSurface, modifier = Modifier.size(22.dp))
                    Text(
                        "SECURE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = ManropeFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            fontSize = 18.sp
                        ),
                        color = OnSurface
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Primary)
                }
            }
        },
        containerColor = SurfaceContainerLowest,
        bottomBar = {
            // Bottom nav from Stitch design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(SurfaceContainer.copy(alpha = 0.6f))
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BottomNavIcon(icon = "grid_view", isActive = true)
                    BottomNavIcon(icon = "emergency", isActive = false)
                    BottomNavIcon(icon = "history", isActive = false)
                    BottomNavIcon(icon = "settings", isActive = false)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (uiState.expression.isNotEmpty()) {
                    Text(
                        text = uiState.expression,
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        ),
                        color = OnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = uiState.displayValue,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp,
                        letterSpacing = (-2).sp
                    ),
                    color = OnSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad
            val buttons = listOf(
                listOf("AC", "+/-", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                buttons.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { symbol ->
                            CalcButton(
                                symbol = symbol,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onButtonClick(symbol) }
                            )
                        }
                    }
                }

                // Last row with wide 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CalcButton(
                        symbol = "0",
                        modifier = Modifier.weight(2f),
                        isWide = true,
                        onClick = { viewModel.onButtonClick("0") }
                    )
                    CalcButton(
                        symbol = ".",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onButtonClick(".") }
                    )
                    CalcButton(
                        symbol = "=",
                        modifier = Modifier.weight(1f),
                        isEquals = true,
                        onClick = { viewModel.onButtonClick("=") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CalcButton(
    symbol: String,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
    isEquals: Boolean = false,
    onClick: () -> Unit
) {
    val isOperator = symbol in listOf("÷", "×", "−", "+")
    val isFunction = symbol in listOf("AC", "+/-", "%")

    val bgColor = when {
        isEquals -> Secondary
        isOperator -> SurfaceContainerHigh
        else -> GlassCardBackground
    }

    val textColor = when {
        isEquals -> OnSecondary
        isOperator -> Secondary
        else -> OnSurface
    }

    val fontWeight = when {
        isOperator || isEquals -> FontWeight.SemiBold
        isFunction -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }

    Box(
        modifier = modifier
            .then(if (!isWide) Modifier.aspectRatio(1f) else Modifier.height(80.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = if (isWide) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = ManropeFontFamily,
                fontWeight = fontWeight,
                fontSize = if (isOperator || isEquals) 28.sp else 24.sp
            ),
            color = textColor,
            modifier = if (isWide) Modifier.padding(start = 32.dp) else Modifier
        )
    }
}

@Composable
private fun BottomNavIcon(icon: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .then(if (isActive) Modifier.background(SurfaceContainerHigh) else Modifier)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Using text as icon placeholder since Material Symbols aren't in Compose icons
        Text(
            text = when (icon) {
                "grid_view" -> "⊞"
                "emergency" -> "⚕"
                "history" -> "⏱"
                "settings" -> "⚙"
                else -> "•"
            },
            color = if (isActive) Secondary else OnSurfaceVariant,
            fontSize = 20.sp
        )
    }
}
