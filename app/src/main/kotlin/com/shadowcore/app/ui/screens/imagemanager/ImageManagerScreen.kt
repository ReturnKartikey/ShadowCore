package com.shadowcore.app.ui.screens.imagemanager

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowcore.app.engine.ImageStatus
import com.shadowcore.app.engine.SystemImage

private val NeonCyan = Color(0xFF00E5FF)
private val NeonGreen = Color(0xFF00E676)
private val NeonOrange = Color(0xFFFF9100)
private val NeonRed = Color(0xFFFF5252)
private val DeepSurface = Color(0xFF0D1117)
private val CardSurface = Color(0xFF161B22)
private val DimText = Color(0xFF8B949E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    onBack: () -> Unit,
    viewModel: ImageManagerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("System Images", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Download Android environments",
                            color = DimText,
                            fontSize = 12.sp,
                        )
                    }
                },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            // Storage info
            item {
                StorageInfoCard(
                    usedMb = state.usedStorageMb,
                    availableMb = state.availableStorageMb,
                    downloadedCount = state.downloadedCount,
                )
            }

            // Image list
            items(state.images, key = { it.image.version }) { imageState ->
                SystemImageCard(
                    image = imageState.image,
                    status = imageState.status,
                    downloadProgress = imageState.downloadProgress,
                    onDownload = { viewModel.downloadImage(imageState.image) },
                    onDelete = { viewModel.deleteImage(imageState.image.version) },
                    onCancel = { viewModel.cancelDownload(imageState.image.version) },
                )
            }
        }
    }
}

@Composable
private fun StorageInfoCard(
    usedMb: Long,
    availableMb: Long,
    downloadedCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent)
                )
            )
            .border(1.dp, NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Storage Used", color = DimText, fontSize = 11.sp)
                Text(
                    "${usedMb / 1024f}GB",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Available", color = DimText, fontSize = 11.sp)
                Text(
                    "${availableMb / 1024f}GB",
                    color = NeonGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Downloaded", color = DimText, fontSize = 11.sp)
                Text(
                    "$downloadedCount images",
                    color = NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SystemImageCard(
    image: SystemImage,
    status: ImageStatus,
    downloadProgress: Float,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    val borderColor = when (status) {
        ImageStatus.READY -> NeonGreen.copy(alpha = 0.3f)
        ImageStatus.DOWNLOADING, ImageStatus.EXTRACTING -> NeonOrange.copy(alpha = 0.3f)
        ImageStatus.ERROR -> NeonRed.copy(alpha = 0.3f)
        else -> Color.White.copy(alpha = 0.06f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Version badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = getVersionColors(image.apiLevel)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        image.version,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        image.displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${image.codeName} • API ${image.apiLevel} • ${image.downloadSizeMb}MB",
                        color = DimText,
                        fontSize = 12.sp,
                    )
                }

                // Status/Action
                when (status) {
                    ImageStatus.NOT_DOWNLOADED -> {
                        FilledTonalButton(
                            onClick = onDownload,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NeonCyan.copy(alpha = 0.15f),
                                contentColor = NeonCyan,
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Icon(Icons.Rounded.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Download", fontSize = 12.sp)
                        }
                    }
                    ImageStatus.DOWNLOADING, ImageStatus.EXTRACTING -> {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Rounded.Close, "Cancel", tint = NeonOrange)
                        }
                    }
                    ImageStatus.READY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    "Delete",
                                    tint = NeonRed.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    ImageStatus.ERROR -> {
                        FilledTonalButton(
                            onClick = onDownload,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NeonRed.copy(alpha = 0.15f),
                                contentColor = NeonRed,
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Retry", fontSize = 12.sp)
                        }
                    }
                    else -> {}
                }
            }

            // Download progress bar
            AnimatedVisibility(
                visible = status == ImageStatus.DOWNLOADING || status == ImageStatus.EXTRACTING,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            if (status == ImageStatus.EXTRACTING) "Extracting..." else "Downloading...",
                            color = NeonOrange,
                            fontSize = 11.sp,
                        )
                        Text(
                            "${(downloadProgress * 100).toInt()}%",
                            color = NeonOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonOrange,
                        trackColor = Color.White.copy(alpha = 0.06f),
                    )
                }
            }
        }
    }
}

private fun getVersionColors(apiLevel: Int): List<Color> = when {
    apiLevel >= 34 -> listOf(Color(0xFF7C4DFF), Color(0xFFB388FF))
    apiLevel >= 33 -> listOf(Color(0xFF00897B), Color(0xFF4DB6AC))
    apiLevel >= 30 -> listOf(Color(0xFF1A73E8), Color(0xFF4FC3F7))
    apiLevel >= 28 -> listOf(Color(0xFFE91E63), Color(0xFFFF6090))
    else -> listOf(Color(0xFFFF6D00), Color(0xFFFFAB40))
}
