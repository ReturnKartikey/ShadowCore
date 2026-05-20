package com.shadowcore.app.ui.screens.activation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowcore.app.engine.ActivationState

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGreen = Color(0xFF00E676)
private val DeepSurface = Color(0xFF0D1117)
private val CardSurface = Color(0xFF161B22)
private val DimText = Color(0xFF8B949E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(
    onBack: () -> Unit,
    onActivated: () -> Unit,
    viewModel: ActivationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.activationState) {
        if (state.activationState is ActivationState.Activated) {
            onActivated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activate ShadowCore", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
        containerColor = DeepSurface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.15f),
                                NeonGreen.copy(alpha = 0.08f),
                            )
                        )
                    )
                    .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "⚡ One-Time Setup",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "ShadowCore needs elevated permissions to run virtual environments. This uses Wireless Debugging — no root required.",
                        color = DimText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }

            // Step 1: Developer Options
            ActivationStepCard(
                stepNumber = 1,
                title = "Enable Developer Options",
                description = "Settings → About Phone → Tap \"Build Number\" 7 times",
                icon = Icons.Rounded.DeveloperMode,
                isCompleted = state.devOptionsEnabled,
                isActive = !state.devOptionsEnabled,
                onAction = { viewModel.checkDeveloperOptions() },
                actionText = "Check Status",
            )

            // Step 2: Wireless Debugging
            ActivationStepCard(
                stepNumber = 2,
                title = "Enable Wireless Debugging",
                description = "Settings → Developer Options → Wireless Debugging → ON\n(Must be connected to WiFi)",
                icon = Icons.Rounded.Wifi,
                isCompleted = state.wirelessDebuggingEnabled,
                isActive = state.devOptionsEnabled && !state.wirelessDebuggingEnabled,
                onAction = { viewModel.checkWirelessDebugging() },
                actionText = "Check Status",
            )

            // Step 3: Install Shizuku
            ActivationStepCard(
                stepNumber = 3,
                title = "Install Shizuku",
                description = "Download Shizuku from Play Store or shizuku.rikka.app",
                icon = Icons.Rounded.InstallMobile,
                isCompleted = state.shizukuInstalled,
                isActive = state.wirelessDebuggingEnabled && !state.shizukuInstalled,
                onAction = { viewModel.checkShizukuInstalled() },
                actionText = "Check Status",
            )

            // Step 4: Start Shizuku
            ActivationStepCard(
                stepNumber = 4,
                title = "Start Shizuku Service",
                description = "Open Shizuku → Tap \"Start\" under Wireless Debugging section → Pair with code",
                icon = Icons.Rounded.PlayArrow,
                isCompleted = state.shizukuRunning,
                isActive = state.shizukuInstalled && !state.shizukuRunning,
                onAction = { viewModel.checkShizukuRunning() },
                actionText = "Check Status",
            )

            // Step 5: Grant Permission
            ActivationStepCard(
                stepNumber = 5,
                title = "Grant Permission",
                description = "Allow ShadowCore to use Shizuku's elevated access",
                icon = Icons.Rounded.Security,
                isCompleted = state.activationState is ActivationState.Activated,
                isActive = state.shizukuRunning && state.activationState !is ActivationState.Activated,
                onAction = { viewModel.requestShizukuPermission() },
                actionText = "Grant Access",
            )

            // Activated banner
            AnimatedVisibility(
                visible = state.activationState is ActivationState.Activated,
                enter = fadeIn() + expandVertically(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NeonGreen.copy(alpha = 0.2f), NeonCyan.copy(alpha = 0.1f))
                            )
                        )
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "ShadowCore Activated!",
                            color = NeonGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "You can now create and run virtual environments.",
                            color = DimText,
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            // Note about reactivation
            Text(
                "Note: You'll need to re-activate after rebooting your phone. Shizuku permissions are temporary.",
                color = DimText.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActivationStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isActive: Boolean,
    onAction: () -> Unit,
    actionText: String,
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    val borderColor = when {
        isCompleted -> NeonGreen.copy(alpha = 0.4f)
        isActive -> NeonCyan.copy(alpha = 0.4f)
        else -> Color.White.copy(alpha = 0.06f)
    }

    val numberBg = when {
        isCompleted -> NeonGreen
        isActive -> NeonCyan
        else -> DimText.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isActive) Modifier.scale(pulseScale) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CardSurface else CardSurface.copy(alpha = 0.5f),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Step number circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(numberBg),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(
                        "$stepNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isActive) NeonCyan else DimText,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        title,
                        color = if (isCompleted) NeonGreen else if (isActive) Color.White else DimText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    description,
                    color = DimText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )

                if (isActive) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.15f),
                            contentColor = NeonCyan,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(actionText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
