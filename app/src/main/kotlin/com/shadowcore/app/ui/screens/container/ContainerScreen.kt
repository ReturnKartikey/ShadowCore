package com.shadowcore.app.ui.screens.container

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGreen = Color(0xFF00E676)
private val NeonPurple = Color(0xFFBB86FC)
private val DeepSurface = Color(0xFF0D1117)
private val CardSurface = Color(0xFF161B22)
private val DimText = Color(0xFF8B949E)
private val StatusBarBg = Color(0xFF010409)

/**
 * Full-screen container display showing the virtual Android environment.
 * Acts as the main interaction surface for the running virtual environment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerScreen(
    onBack: () -> Unit,
    viewModel: ContainerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSurface),
    ) {
        // Virtual Status Bar
        VirtualStatusBar(
            androidVersion = state.androidVersion,
            isRunning = state.isRunning,
            onBack = onBack,
        )

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                state.isBooting -> {
                    BootingOverlay(
                        bootPhase = state.bootPhase,
                        bootProgress = state.bootProgress,
                    )
                }
                state.isRunning -> {
                    VirtualLauncher(
                        installedApps = state.installedApps,
                        onAppClick = { viewModel.launchApp(it) },
                        onInstallApp = { viewModel.showInstallDialog() },
                    )
                }
                else -> {
                    StoppedOverlay(onStart = { viewModel.startContainer() })
                }
            }
        }

        // Virtual Navigation Bar
        VirtualNavBar(
            onBack = { viewModel.onVirtualBack() },
            onHome = { viewModel.onVirtualHome() },
            onRecents = { viewModel.onVirtualRecents() },
            isRunning = state.isRunning,
        )
    }
}

@Composable
private fun VirtualStatusBar(
    androidVersion: String,
    isRunning: Boolean,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StatusBarBg)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Exit Container",
                tint = DimText,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        // Running indicator
        if (isRunning) {
            val pulseAnim = rememberInfiniteTransition(label = "statusPulse")
            val alpha by pulseAnim.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "statusAlpha",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = alpha)),
            )
            Spacer(Modifier.width(6.dp))
        }

        Text(
            text = "ShadowCore • $androidVersion",
            color = DimText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.weight(1f))

        // Status icons
        Icon(Icons.Rounded.Wifi, null, tint = DimText, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Rounded.BatteryFull, null, tint = DimText, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun VirtualLauncher(
    installedApps: List<VirtualApp>,
    onAppClick: (String) -> Unit,
    onInstallApp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Welcome header
        Text(
            "Virtual Environment",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            "${installedApps.size} apps installed",
            color = DimText,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        // App grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
        ) {
            // Install new app button
            item {
                AppGridItem(
                    label = "Install APK",
                    icon = Icons.Rounded.Add,
                    iconColor = NeonCyan,
                    onClick = onInstallApp,
                    isSpecial = true,
                )
            }

            // Settings
            item {
                AppGridItem(
                    label = "Settings",
                    icon = Icons.Rounded.Settings,
                    iconColor = DimText,
                    onClick = { /* TODO: Virtual settings */ },
                )
            }

            // File Manager
            item {
                AppGridItem(
                    label = "Files",
                    icon = Icons.Rounded.Folder,
                    iconColor = Color(0xFFFFB74D),
                    onClick = { /* TODO */ },
                )
            }

            // Terminal
            item {
                AppGridItem(
                    label = "Terminal",
                    icon = Icons.Rounded.Code,
                    iconColor = NeonGreen,
                    onClick = { /* TODO */ },
                )
            }

            // Installed apps
            items(installedApps) { app ->
                AppGridItem(
                    label = app.appName,
                    icon = Icons.Rounded.Apps,
                    iconColor = NeonPurple,
                    onClick = { onAppClick(app.packageName) },
                )
            }
        }
    }
}

@Composable
private fun AppGridItem(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    isSpecial: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSpecial) NeonCyan.copy(alpha = 0.1f)
                    else CardSurface
                )
                .then(
                    if (isSpecial) Modifier.border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BootingOverlay(
    bootPhase: String,
    bootProgress: Float,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Animated boot icon
            val rotation by rememberInfiniteTransition(label = "bootSpin").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                ),
                label = "bootRotation",
            )

            Icon(
                Icons.Rounded.Memory,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer { rotationZ = rotation },
            )

            Text(
                "Booting Virtual Environment",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                bootPhase,
                color = NeonCyan,
                fontSize = 13.sp,
            )

            LinearProgressIndicator(
                progress = { bootProgress },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = NeonCyan,
                trackColor = CardSurface,
            )
        }
    }
}

@Composable
private fun StoppedOverlay(
    onStart: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                tint = DimText,
                modifier = Modifier.size(72.dp),
            )
            Text(
                "Virtual Environment Stopped",
                color = DimText,
                fontSize = 16.sp,
            )
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan.copy(alpha = 0.15f),
                    contentColor = NeonCyan,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun VirtualNavBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit,
    isRunning: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StatusBarBg)
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back
        IconButton(onClick = onBack, enabled = isRunning) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = if (isRunning) Color.White.copy(alpha = 0.7f) else DimText.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp),
            )
        }

        // Home
        IconButton(onClick = onHome, enabled = isRunning) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (isRunning) Color.White.copy(alpha = 0.7f) else DimText.copy(alpha = 0.3f),
                        CircleShape,
                    ),
            )
        }

        // Recents
        IconButton(onClick = onRecents, enabled = isRunning) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(
                        2.dp,
                        if (isRunning) Color.White.copy(alpha = 0.7f) else DimText.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

/**
 * Data class representing an app installed inside the virtual environment.
 */
data class VirtualApp(
    val packageName: String,
    val appName: String,
    val versionName: String = "",
)
