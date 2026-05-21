package com.shadowcore.app.ui.screens.createvm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowcore.app.domain.model.AndroidVersion
import com.shadowcore.app.domain.model.VeCategory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateVmScreen(
    onBack: () -> Unit,
    viewModel: CreateVmViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCreated) {
        if (state.isCreated) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Virtual Environment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Name input
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("Environment Name") },
                placeholder = { Text("e.g., Work Phone, Gaming") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = state.errorMessage != null && state.name.isBlank(),
            )

            // Android Version Selection — all unlocked
            Text("Android Version", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AndroidVersion.entries.forEach { version ->
                    val isSelected = state.selectedVersion == version
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
                            .background(bgColor)
                            .clickable { viewModel.selectVersion(version) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(version.displayName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(version.codeName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Category Selection
            Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VeCategory.entries.forEach { category ->
                    val isSelected = state.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.displayName) },
                        leadingIcon = {
                            Icon(category.icon, null, Modifier.size(16.dp))
                        },
                    )
                }
            }

            // RAM Slider
            Text("RAM: ${state.allocatedRamMb}MB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Slider(
                value = state.allocatedRamMb.toFloat(),
                onValueChange = { viewModel.updateRam(it.toInt()) },
                valueRange = 512f..8192f,
                steps = 14,
            )

            // Storage Slider
            Text("Storage: %.1fGB".format(state.allocatedStorageMb / 1024f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Slider(
                value = state.allocatedStorageMb.toFloat(),
                onValueChange = { viewModel.updateStorage(it.toInt()) },
                valueRange = 2048f..32768f,
                steps = 14,
            )

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Network Isolation", style = MaterialTheme.typography.titleSmall)
                    Text("Block internet for this environment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.isNetworkIsolated, onCheckedChange = { viewModel.toggleNetworkIsolation() })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Root Access", style = MaterialTheme.typography.titleSmall)
                    Text("Enable root inside environment (host stays safe)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.hasRootAccess, onCheckedChange = { viewModel.toggleRootAccess() })
            }

            // Error
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Create button
            Button(
                onClick = viewModel::createVe,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !state.isCreating,
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Virtual Environment", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
