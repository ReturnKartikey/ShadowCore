package com.shadowcore.app.engine

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of available Android system images.
 *
 * This singleton maintains the catalog of system images that can be downloaded
 * and used to boot virtual environment containers. Each entry includes metadata
 * such as API level, download URL, size, and integrity hash.
 *
 * Currently uses hardcoded entries with placeholder Firebase Storage URLs.
 * In production, this catalog could be fetched from a remote config endpoint.
 *
 * ## Supported Images
 * | Version | API | Code Name          |
 * |---------|-----|--------------------|
 * | 9       | 28  | Pie                |
 * | 11      | 30  | Red Velvet Cake    |
 * | 13      | 33  | Tiramisu           |
 * | 15      | 35  | Vanilla Ice Cream  |
 */
@Singleton
class ImageRegistry @Inject constructor() {

    /**
     * All available system images that can be downloaded.
     */
    val availableImages: List<SystemImage> = listOf(
        SystemImage(
            version = "9",
            apiLevel = 28,
            displayName = "Android 9",
            codeName = "Pie",
            downloadUrl = "https://firebasestorage.googleapis.com/v0/b/shadowcore-prod.appspot.com/o/images%2Fandroid-9-system.tar.gz?alt=media",
            downloadSizeMb = 320,
            sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            minHostApiLevel = 29,
        ),
        SystemImage(
            version = "11",
            apiLevel = 30,
            displayName = "Android 11",
            codeName = "Red Velvet Cake",
            downloadUrl = "https://firebasestorage.googleapis.com/v0/b/shadowcore-prod.appspot.com/o/images%2Fandroid-11-system.tar.gz?alt=media",
            downloadSizeMb = 380,
            sha256 = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2",
            minHostApiLevel = 29,
        ),
        SystemImage(
            version = "13",
            apiLevel = 33,
            displayName = "Android 13",
            codeName = "Tiramisu",
            downloadUrl = "https://firebasestorage.googleapis.com/v0/b/shadowcore-prod.appspot.com/o/images%2Fandroid-13-system.tar.gz?alt=media",
            downloadSizeMb = 450,
            sha256 = "b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3",
            minHostApiLevel = 29,
        ),
        SystemImage(
            version = "15",
            apiLevel = 35,
            displayName = "Android 15",
            codeName = "Vanilla Ice Cream",
            downloadUrl = "https://firebasestorage.googleapis.com/v0/b/shadowcore-prod.appspot.com/o/images%2Fandroid-15-system.tar.gz?alt=media",
            downloadSizeMb = 500,
            sha256 = "c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4",
            minHostApiLevel = 29,
        ),
    )

    /**
     * Finds a system image by its Android version string.
     *
     * @param version Android version (e.g., "15")
     * @return The matching [SystemImage], or `null` if not found
     */
    fun getImage(version: String): SystemImage? =
        availableImages.find { it.version == version }

    /**
     * Finds a system image by its Android API level.
     *
     * @param apiLevel Android API level (e.g., 35)
     * @return The matching [SystemImage], or `null` if not found
     */
    fun getImageForApiLevel(apiLevel: Int): SystemImage? =
        availableImages.find { it.apiLevel == apiLevel }

    /**
     * Returns a list of all supported API levels.
     */
    fun supportedApiLevels(): List<Int> =
        availableImages.map { it.apiLevel }

    /**
     * Checks if the host device meets the minimum API level for a given image.
     *
     * @param version Android version string
     * @return `true` if the host device can run the image
     */
    fun isCompatibleWithHost(version: String): Boolean {
        val image = getImage(version) ?: return false
        return android.os.Build.VERSION.SDK_INT >= image.minHostApiLevel
    }
}
