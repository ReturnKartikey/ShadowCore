package com.shadowcore.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shadowcore.app.domain.model.AndroidVersion

@Composable
fun AndroidVersionBadge(
    version: AndroidVersion,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (version) {
        AndroidVersion.PIE_9 -> Color(0xFF4CAF50) to Color.White
        AndroidVersion.R_11 -> Color(0xFF2196F3) to Color.White
        AndroidVersion.TIRAMISU_13 -> Color(0xFFFF9800) to Color.Black
        AndroidVersion.VANILLA_ICE_CREAM_15 -> Color(0xFFE91E63) to Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = version.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
