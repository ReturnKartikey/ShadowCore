package com.shadowcore.app.domain.model

/**
 * Supported Android versions that can run inside a Virtual Environment.
 * All versions are available to all users — no premium gating.
 */
enum class AndroidVersion(
    val apiLevel: Int,
    val displayName: String,
    val codeName: String,
    val estimatedImageSizeMb: Int,
    val downloadSizeMb: Int,
    val downloadUrl: String,
) {
    PIE_9(
        apiLevel = 28,
        displayName = "Android 9",
        codeName = "Pie",
        estimatedImageSizeMb = 1200,
        downloadSizeMb = 350,
        downloadUrl = "",
    ),

    R_11(
        apiLevel = 30,
        displayName = "Android 11",
        codeName = "R",
        estimatedImageSizeMb = 1800,
        downloadSizeMb = 400,
        downloadUrl = "",
    ),

    TIRAMISU_13(
        apiLevel = 33,
        displayName = "Android 13",
        codeName = "Tiramisu",
        estimatedImageSizeMb = 2400,
        downloadSizeMb = 450,
        downloadUrl = "",
    ),

    VANILLA_ICE_CREAM_15(
        apiLevel = 35,
        displayName = "Android 15",
        codeName = "Vanilla Ice Cream",
        estimatedImageSizeMb = 3000,
        downloadSizeMb = 500,
        downloadUrl = "",
    );

    val fullDisplayName: String get() = "$displayName $codeName"
}
