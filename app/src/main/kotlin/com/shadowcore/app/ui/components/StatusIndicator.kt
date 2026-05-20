package com.shadowcore.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shadowcore.app.domain.model.VmState
import com.shadowcore.app.ui.theme.*

@Composable
fun StatusIndicator(
    state: VmState,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = when (state) {
            is VmState.Running -> VmRunning
            is VmState.Starting -> VmDownloading
            is VmState.Paused -> VmPaused
            is VmState.Downloading -> VmDownloading
            is VmState.Error -> VmError
            is VmState.Stopped -> VmStopped
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    val isPulsing = state is VmState.Running || state is VmState.Starting

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Pulse ring
        if (isPulsing) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f))
            )
        }
        // Solid dot
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}
