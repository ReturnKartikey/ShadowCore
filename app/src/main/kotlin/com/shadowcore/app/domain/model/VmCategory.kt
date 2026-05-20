package com.shadowcore.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Predefined VM profile categories with associated icons and colors.
 */
enum class VmCategory(
    val displayName: String,
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color,
) {
    WORK(
        displayName = "Work",
        icon = Icons.Rounded.Business,
        gradientStart = Color(0xFF1A73E8),
        gradientEnd = Color(0xFF4FC3F7),
    ),

    GAMING(
        displayName = "Gaming",
        icon = Icons.Rounded.Gamepad,
        gradientStart = Color(0xFFE91E63),
        gradientEnd = Color(0xFFFF6090),
    ),

    PRIVACY(
        displayName = "Privacy",
        icon = Icons.Rounded.Lock,
        gradientStart = Color(0xFF00897B),
        gradientEnd = Color(0xFF4DB6AC),
    ),

    PERSONAL(
        displayName = "Personal",
        icon = Icons.Rounded.Person,
        gradientStart = Color(0xFF7C4DFF),
        gradientEnd = Color(0xFFB388FF),
    ),

    CUSTOM(
        displayName = "Custom",
        icon = Icons.Rounded.Tune,
        gradientStart = Color(0xFFFF6D00),
        gradientEnd = Color(0xFFFFAB40),
    );
}
