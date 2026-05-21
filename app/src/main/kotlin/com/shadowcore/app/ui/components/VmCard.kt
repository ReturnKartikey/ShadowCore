package com.shadowcore.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shadowcore.app.domain.model.VeProfile
import com.shadowcore.app.domain.model.VeState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VmCard(
    profile: VeProfile,
    onClick: () -> Unit,
    onQuickStart: (() -> Unit)? = null,
    onQuickStop: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            profile.category.gradientStart.copy(alpha = 0.15f),
            profile.category.gradientEnd.copy(alpha = 0.05f),
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box {
            // Gradient overlay based on category
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(gradient)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Top row: Name + Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = profile.category.icon,
                            contentDescription = null,
                            tint = profile.category.gradientStart,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    StatusIndicator(state = profile.state, size = 10.dp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Android version badge + state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AndroidVersionBadge(version = profile.androidVersion)
                    Text(
                        text = profile.state.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resource bars
                ResourceBar(
                    label = "RAM",
                    usedText = "${profile.usedRamMb}MB",
                    totalText = "${profile.allocatedRamMb}MB",
                    progress = profile.ramUsagePercent,
                    gradientColors = listOf(
                        profile.category.gradientStart,
                        profile.category.gradientEnd,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                ResourceBar(
                    label = "Storage",
                    usedText = "%.1fGB".format(profile.usedStorageGb),
                    totalText = "%.1fGB".format(profile.allocatedStorageGb),
                    progress = profile.storageUsagePercent,
                )

                // Quick action buttons
                if (onQuickStart != null || onQuickStop != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (profile.lastUsedAt > 0) {
                            Text(
                                text = formatTime(profile.lastUsedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }

                        if (profile.state.canStart && onQuickStart != null) {
                            FilledTonalIconButton(onClick = onQuickStart) {
                                Icon(Icons.Rounded.PlayArrow, "Start", Modifier.size(20.dp))
                            }
                        }
                        if (profile.state.canStop && onQuickStop != null) {
                            FilledTonalIconButton(onClick = onQuickStop) {
                                Icon(Icons.Rounded.Stop, "Stop", Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
