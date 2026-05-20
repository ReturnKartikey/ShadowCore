package com.shadowcore.app.ui.screens.vmconsole

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowcore.app.domain.model.VmState

// Console color palette
private val ConsoleBg = Color(0xFF0D1117)
private val ConsoleText = Color(0xFFC9D1D9)
private val ConsoleGreen = Color(0xFF3FB950)
private val ConsoleYellow = Color(0xFFD29922)
private val ConsoleRed = Color(0xFFf85149)
private val ConsoleCyan = Color(0xFF58A6FF)
private val ConsoleGray = Color(0xFF484F58)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmConsoleScreen(
    onBack: () -> Unit,
    viewModel: VmConsoleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom as new logs appear
    LaunchedEffect(state.logs.size) {
        if (state.logs.isNotEmpty()) {
            listState.animateScrollToItem(state.logs.size - 1)
        }
    }

    // Auto-start boot when profile loads and VM is not running
    val profile = state.profile
    LaunchedEffect(profile) {
        if (profile != null && profile.state.canStart && !state.isBooting) {
            viewModel.startBoot()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.profile?.name ?: "VM Console",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            when {
                                state.isBooting -> "Booting..."
                                state.isRunning -> "Running"
                                state.errorMessage != null -> "Error"
                                else -> state.profile?.state?.displayName ?: "Idle"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                state.isBooting -> ConsoleYellow
                                state.isRunning -> ConsoleGreen
                                state.errorMessage != null -> ConsoleRed
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.isRunning) {
                        FilledTonalIconButton(onClick = viewModel::stopVm) {
                            Icon(Icons.Rounded.Stop, "Stop VM", tint = ConsoleRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ConsoleBg,
                    titleContentColor = ConsoleText,
                    navigationIconContentColor = ConsoleText,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ConsoleBg),
        ) {
            // Boot progress bar
            AnimatedVisibility(visible = state.isBooting) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Boot Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = ConsoleGray,
                        )
                        Text(
                            "${(state.bootProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ConsoleCyan,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.bootProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ConsoleCyan,
                        trackColor = ConsoleGray.copy(alpha = 0.3f),
                    )
                }
            }

            // Running status bar
            AnimatedVisibility(visible = state.isRunning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ConsoleGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(ConsoleGreen, shape = CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "VM Running",
                            color = ConsoleGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    state.profile?.let { profile ->
                        Text(
                            "RAM: ${profile.usedRamMb}MB / ${profile.allocatedRamMb}MB",
                            color = ConsoleGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }

            // Console log output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(state.logs, key = { "${it.timestamp}_${it.message}" }) { entry ->
                    ConsoleLogLine(entry)
                }

                // Blinking cursor at end
                if (state.isBooting || state.isRunning) {
                    item {
                        Text(
                            "▋",
                            color = ConsoleGreen,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleLogLine(entry: ConsoleLogEntry) {
    val color = when (entry.level) {
        LogLevel.INFO -> ConsoleText
        LogLevel.SUCCESS -> ConsoleGreen
        LogLevel.WARN -> ConsoleYellow
        LogLevel.ERROR -> ConsoleRed
        LogLevel.SYSTEM -> ConsoleCyan
    }

    val prefix = when (entry.level) {
        LogLevel.INFO -> "  "
        LogLevel.SUCCESS -> "✓ "
        LogLevel.WARN -> "⚠ "
        LogLevel.ERROR -> "✗ "
        LogLevel.SYSTEM -> "» "
    }

    Text(
        text = "$prefix${entry.message}",
        color = color,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 1.dp, horizontal = 4.dp),
    )
}
