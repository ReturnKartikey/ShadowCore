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
 * Images are hosted as GitHub Release assets at:
 * https://github.com/ReturnKartikey/ShadowCore/releases/download/v1.0-images/
 *
 * ## Supported Images
 * | Version | API | Code Name          | Size  |
 * |---------|-----|--------------------| ------|
 * | 9       | 28  | Pie                | ~320MB|
 * | 11      | 30  | Red Velvet Cake    | ~380MB|
 * | 13      | 33  | Tiramisu           | ~450MB|
 * | 15      | 35  | Vanilla Ice Cream  | ~500MB|
 */
@Singleton
class ImageRegistry @Inject constructor() {

    companion object {
        private const val GITHUB_RELEASE_BASE =
            "https://github.com/ReturnKartikey/ShadowCore/releases/download/v1.0-images"
    }

    /**
     * All available system images that can be downloaded.
     */
    val availableImages: List<SystemImage> = listOf(
        SystemImage(
            version = "9",
            apiLevel = 28,
            displayName = "Android 9",
            codeName = "Pie",
            downloadUrl = "$GITHUB_RELEASE_BASE/android-9-system.tar.gz",
            downloadSizeMb = 320,
            sha256 = "", // Will be set after actual images are built
            minHostApiLevel = 29,
        ),
        SystemImage(
            version = "11",
            apiLevel = 30,
            displayName = "Android 11",
            codeName = "Red Velvet Cake",
            downloadUrl = "$GITHUB_RELEASE_BASE/android-11-system.tar.gz",
            downloadSizeMb = 380,
            sha256 = "",
            minHostApiLevel = 29,
        ),
        SystemImage(
            version = "13",
            apiLevel = 33,
            displayName = "Android 13",
            codeName = "Tiramisu",
            downloadUrl = "$GITHUB_RELEASE_BASE/android-13-system.tar.gz",
            downloadSizeMb = 450,
            sha256 = "",
            minHostApiLevel = 29,
        ),
        SystemImage(
            version = "15",
            apiLevel = 35,
            displayName = "Android 15",
            codeName = "Vanilla Ice Cream",
            downloadUrl = "$GITHUB_RELEASE_BASE/android-15-system.tar.gz",
            downloadSizeMb = 500,
            sha256 = "",
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
