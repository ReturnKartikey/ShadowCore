package com.shadowcore.app.ui.screens.vmdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.ui.components.AndroidVersionBadge
import com.shadowcore.app.ui.components.ResourceBar
import com.shadowcore.app.ui.components.StatusIndicator
import com.shadowcore.app.ui.theme.VmRunning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmDetailScreen(
    onBack: () -> Unit,
    onNavigateToConsole: (String) -> Unit,
    viewModel: VmDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.profile?.name ?: "VM Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        val profile = state.profile

        if (state.isLoading || profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(profile.category.icon, null, Modifier.size(32.dp), tint = profile.category.gradientStart)
                        Column(Modifier.weight(1f)) {
                            Text(profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AndroidVersionBadge(version = profile.androidVersion)
                                Text("• ${profile.executionTier.displayName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusIndicator(state = profile.state, size = 16.dp)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Running status banner — tap to open console
                    if (profile.state is VmState.Running) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            onClick = { onNavigateToConsole(profile.id) },
                            colors = CardDefaults.cardColors(containerColor = VmRunning.copy(alpha = 0.12f)),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Rounded.Code, null, tint = VmRunning, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("VM is running — tap to open console", fontWeight = FontWeight.SemiBold, color = VmRunning, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    ResourceBar(label = "RAM", usedText = "${profile.usedRamMb}MB", totalText = "${profile.allocatedRamMb}MB", progress = profile.ramUsagePercent)
                    Spacer(Modifier.height(8.dp))
                    ResourceBar(label = "Storage", usedText = "%.1fGB".format(profile.usedStorageGb), totalText = "%.1fGB".format(profile.allocatedStorageGb), progress = profile.storageUsagePercent)
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (profile.state.canStart) {
                    Button(
                        onClick = { onNavigateToConsole(profile.id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Start VM")
                    }
                }
                if (profile.state is VmState.Running) {
                    Button(
                        onClick = { onNavigateToConsole(profile.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = VmRunning),
                    ) {
                        Icon(Icons.Rounded.Code, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Open Console")
                    }
                }
                if (profile.state.canStop) {
                    OutlinedButton(onClick = viewModel::stopVm, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Stop, null); Spacer(Modifier.width(4.dp)); Text("Stop")
                    }
                }
                if (profile.state.canPause) {
                    OutlinedButton(onClick = viewModel::pauseVm, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Pause, null); Spacer(Modifier.width(4.dp)); Text("Pause")
                    }
                }
            }

            // Settings section
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    SettingRow("Network Isolation", if (profile.isNetworkIsolated) "Enabled" else "Disabled")
                    SettingRow("Root Access", if (profile.hasRootAccess) "Enabled" else "Disabled")
                    SettingRow("Category", profile.category.displayName)
                    SettingRow("Capability Tier", profile.executionTier.displayName)
                    SettingRow("Android Version", profile.androidVersion.fullDisplayName)
                }
            }

            // Maintenance
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Maintenance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    TextButton(onClick = viewModel::repairVm, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Build, null); Spacer(Modifier.width(8.dp)); Text("Repair VM")
                    }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Rounded.Delete, null); Spacer(Modifier.width(8.dp)); Text("Delete VM")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete VM?") },
            text = { Text("This will permanently delete \"${state.profile?.name}\". This action cannot be undone.") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false; viewModel.deleteVm() }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
