package com.shadowcore.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism card effect for the premium screen.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier) {
        // Glass background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
                )
                .blur(20.dp)
        )

        // Content
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                )
                .padding(24.dp),
            content = content,
        )
    }
}
