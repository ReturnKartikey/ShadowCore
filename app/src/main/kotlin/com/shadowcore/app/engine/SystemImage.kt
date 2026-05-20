package com.shadowcore.app.engine

/**
 * Metadata for a downloadable Android system image.
 *
 * System images are extracted from Pixel factory ROMs and hosted on Firebase Storage.
 * Each image contains: framework JARs, system APKs, native libs, and build.prop.
 * Typical size is 300–500 MB as a compressed tar.gz archive.
 *
 * @property version Android version string (e.g., "15")
 * @property apiLevel Android API level (e.g., 35)
 * @property displayName Human-readable name (e.g., "Android 15")
 * @property codeName Android dessert codename (e.g., "Vanilla Ice Cream")
 * @property downloadUrl Firebase Storage URL for the tar.gz archive
 * @property downloadSizeMb Approximate download size in megabytes
 * @property sha256 SHA-256 hash of the tar.gz archive for integrity verification
 * @property minHostApiLevel Minimum Android API level required on the host device
 */
data class SystemImage(
    val version: String,
    val apiLevel: Int,
    val displayName: String,
    val codeName: String,
    val downloadUrl: String,
    val downloadSizeMb: Int,
    val sha256: String,
    val minHostApiLevel: Int = 29,
)

/**
 * Lifecycle status of a system image on disk.
 */
enum class ImageStatus {
    /** Image has not been downloaded yet. */
    NOT_DOWNLOADED,

    /** Image archive is currently being downloaded. */
    DOWNLOADING,

    /** Archive download is complete but not yet extracted. */
    DOWNLOADED,

    /** Archive is being extracted to the images directory. */
    EXTRACTING,

    /** Image is fully extracted and ready for use by a container. */
    READY,

    /** An error occurred during download, verification, or extraction. */
    ERROR,
}

/**
 * Progress snapshot for an ongoing image download/extraction operation.
 *
 * Emitted via [kotlinx.coroutines.flow.Flow] from [ImageManager.downloadImage].
 *
 * @property version The system image version this progress relates to
 * @property status Current phase of the download pipeline
 * @property downloadedBytes Bytes downloaded so far
 * @property totalBytes Total expected bytes (0 if unknown)
 * @property errorMessage Human-readable error description when [status] is [ImageStatus.ERROR]
 */
data class ImageDownloadProgress(
    val version: String,
    val status: ImageStatus,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val errorMessage: String? = null,
) {
    /** Download completion as a fraction from 0.0 to 1.0. Returns 0 if total size is unknown. */
    val progressPercent: Float
        get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
}
