package com.shadowcore.app.engine

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages downloading, verifying, extracting, and deleting system images.
 *
 * System images are stored under `{app_internal}/images/{version}/`.
 * The download pipeline is:
 * 1. Check if the image is already downloaded (by version directory presence)
 * 2. Download the tar.gz archive from Firebase Storage
 * 3. Verify the SHA-256 hash against the expected value
 * 4. Extract the archive contents to the version directory
 * 5. Clean up the temporary archive file
 *
 * Progress is emitted as [ImageDownloadProgress] via [Flow].
 *
 * @property context Application context for accessing internal storage
 */
@Singleton
class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "ImageManager"
        private const val IMAGES_DIR = "images"
        private const val ARCHIVE_SUFFIX = "-system.tar.gz"
        private const val BUFFER_SIZE = 8 * 1024 // 8 KB
        private const val READY_MARKER = ".ready"
    }

    /** Root directory for all system images. */
    private val imageDir = File(context.filesDir, IMAGES_DIR)

    init {
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
    }

    /**
     * Downloads, verifies, and extracts a system image.
     *
     * Emits [ImageDownloadProgress] events through the returned [Flow].
     * The flow completes when the image is fully ready, or emits an
     * [ImageStatus.ERROR] if anything goes wrong.
     *
     * If the image is already downloaded and ready, emits a single
     * [ImageStatus.READY] event immediately.
     *
     * @param image The [SystemImage] metadata to download
     * @return Flow of progress events
     */
    fun downloadImage(image: SystemImage): Flow<ImageDownloadProgress> = flow {
        val version = image.version
        val versionDir = File(imageDir, version)

        // ── Already ready? ────────────────────────────────────────────
        if (isImageReady(version)) {
            Log.i(TAG, "Image $version is already ready at ${versionDir.absolutePath}")
            emit(ImageDownloadProgress(version, ImageStatus.READY))
            return@flow
        }

        val archiveFile = File(imageDir, "$version$ARCHIVE_SUFFIX")

        try {
            // ── Download ──────────────────────────────────────────────
            emit(ImageDownloadProgress(version, ImageStatus.DOWNLOADING))
            Log.i(TAG, "Starting download of image $version from ${image.downloadUrl}")

            val url = URL(image.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw ImageDownloadException(
                    "HTTP ${connection.responseCode}: ${connection.responseMessage}"
                )
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.buffered(BUFFER_SIZE).use { input ->
                FileOutputStream(archiveFile).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        emit(
                            ImageDownloadProgress(
                                version = version,
                                status = ImageStatus.DOWNLOADING,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                            )
                        )
                    }
                }
            }

            emit(
                ImageDownloadProgress(
                    version = version,
                    status = ImageStatus.DOWNLOADED,
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes,
                )
            )
            Log.i(TAG, "Download complete: ${archiveFile.length()} bytes")

            // ── Verify SHA-256 ────────────────────────────────────────
            Log.i(TAG, "Verifying SHA-256 for image $version")
            val actualHash = computeSha256(archiveFile)
            if (!actualHash.equals(image.sha256, ignoreCase = true)) {
                archiveFile.delete()
                throw ImageVerificationException(
                    "SHA-256 mismatch: expected=${image.sha256}, actual=$actualHash"
                )
            }
            Log.i(TAG, "SHA-256 verified successfully")

            // ── Extract ───────────────────────────────────────────────
            emit(ImageDownloadProgress(version, ImageStatus.EXTRACTING))
            Log.i(TAG, "Extracting archive to ${versionDir.absolutePath}")

            versionDir.mkdirs()
            extractTarGz(archiveFile, versionDir)

            // Write ready marker
            File(versionDir, READY_MARKER).createNewFile()

            // Clean up archive
            archiveFile.delete()
            Log.i(TAG, "Extraction complete, archive cleaned up")

            emit(ImageDownloadProgress(version, ImageStatus.READY))

        } catch (e: Exception) {
            Log.e(TAG, "Failed to download/extract image $version", e)
            // Clean up partial downloads
            archiveFile.delete()
            emit(
                ImageDownloadProgress(
                    version = version,
                    status = ImageStatus.ERROR,
                    errorMessage = e.message ?: "Unknown error",
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Checks whether a system image version is fully extracted and ready to use.
     *
     * @param version The Android version string (e.g., "15")
     * @return `true` if the image directory exists and contains the ready marker
     */
    fun isImageReady(version: String): Boolean {
        val versionDir = File(imageDir, version)
        return versionDir.exists() && File(versionDir, READY_MARKER).exists()
    }

    /**
     * Returns the path to an extracted system image directory.
     *
     * @param version The Android version string
     * @return The [File] pointing to the image directory, or `null` if not ready
     */
    fun getImagePath(version: String): File? {
        val dir = File(imageDir, version)
        return if (isImageReady(version)) dir else null
    }

    /**
     * Deletes a downloaded/extracted system image and frees disk space.
     *
     * @param version The Android version string to delete
     */
    fun deleteImage(version: String) {
        val versionDir = File(imageDir, version)
        if (versionDir.exists()) {
            versionDir.deleteRecursively()
            Log.i(TAG, "Deleted image $version from ${versionDir.absolutePath}")
        }

        // Also clean up any leftover archive
        val archiveFile = File(imageDir, "$version$ARCHIVE_SUFFIX")
        if (archiveFile.exists()) {
            archiveFile.delete()
        }
    }

    /**
     * Returns a list of version strings for all downloaded (ready) images.
     */
    fun getDownloadedImages(): List<String> {
        if (!imageDir.exists()) return emptyList()
        return imageDir.listFiles()
            ?.filter { it.isDirectory && File(it, READY_MARKER).exists() }
            ?.map { it.name }
            ?: emptyList()
    }

    /**
     * Verifies the SHA-256 hash of a downloaded image archive.
     *
     * @param version The Android version string
     * @param expectedSha256 Expected SHA-256 hex string
     * @return `true` if the archive exists and its hash matches
     */
    suspend fun verifyImage(version: String, expectedSha256: String): Boolean =
        withContext(Dispatchers.IO) {
            val archiveFile = File(imageDir, "$version$ARCHIVE_SUFFIX")
            if (!archiveFile.exists()) return@withContext false
            val actualHash = computeSha256(archiveFile)
            actualHash.equals(expectedSha256, ignoreCase = true)
        }

    // ── Private helpers ─────────────────────────────────────────────────

    /**
     * Extracts a tar.gz archive to the specified destination directory.
     *
     * Uses [GZIPInputStream] for decompression and a basic tar entry parser
     * for extraction. This supports regular files and directories only.
     */
    private suspend fun extractTarGz(archive: File, destDir: File) =
        withContext(Dispatchers.IO) {
            GZIPInputStream(BufferedInputStream(FileInputStream(archive))).use { gzipIn ->
                // Basic tar extraction — reads 512-byte tar headers
                val headerBuffer = ByteArray(512)
                while (true) {
                    val headerBytesRead = readFully(gzipIn, headerBuffer)
                    if (headerBytesRead < 512) break

                    // Check for end-of-archive (two consecutive zero blocks)
                    if (headerBuffer.all { it == 0.toByte() }) break

                    // Parse file name (bytes 0–99)
                    val fileName = String(headerBuffer, 0, 100)
                        .trim('\u0000', ' ')
                    if (fileName.isEmpty()) break

                    // Parse file size (bytes 124–135, octal ASCII)
                    val sizeStr = String(headerBuffer, 124, 12)
                        .trim('\u0000', ' ')
                    val fileSize = if (sizeStr.isNotEmpty()) {
                        sizeStr.toLongOrNull(8) ?: 0L
                    } else {
                        0L
                    }

                    // Parse type flag (byte 156)
                    val typeFlag = headerBuffer[156]

                    val outFile = File(destDir, fileName)

                    // Prevent path traversal attacks
                    if (!outFile.canonicalPath.startsWith(destDir.canonicalPath)) {
                        Log.w(TAG, "Skipping entry with path traversal: $fileName")
                        skipBytes(gzipIn, fileSize)
                        continue
                    }

                    when (typeFlag.toInt().toChar()) {
                        '5', '/' -> {
                            // Directory entry
                            outFile.mkdirs()
                        }
                        '0', '\u0000' -> {
                            // Regular file
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { fos ->
                                var remaining = fileSize
                                val buf = ByteArray(BUFFER_SIZE)
                                while (remaining > 0) {
                                    val toRead = minOf(remaining, buf.size.toLong()).toInt()
                                    val read = gzipIn.read(buf, 0, toRead)
                                    if (read == -1) break
                                    fos.write(buf, 0, read)
                                    remaining -= read
                                }
                            }
                            // Tar entries are padded to 512-byte boundaries
                            val padding = (512 - (fileSize % 512)) % 512
                            skipBytes(gzipIn, padding)
                        }
                        else -> {
                            // Skip unsupported entry types (symlinks, etc.)
                            skipBytes(gzipIn, fileSize)
                        }
                    }
                }
            }
        }

    /**
     * Reads exactly [buffer.size] bytes from [input], returning the count actually read.
     */
    private fun readFully(input: java.io.InputStream, buffer: ByteArray): Int {
        var totalRead = 0
        while (totalRead < buffer.size) {
            val read = input.read(buffer, totalRead, buffer.size - totalRead)
            if (read == -1) break
            totalRead += read
        }
        return totalRead
    }

    /**
     * Skips [count] bytes in the input stream, accounting for tar padding.
     */
    private fun skipBytes(input: java.io.InputStream, count: Long) {
        var remaining = count
        val buf = ByteArray(BUFFER_SIZE)
        while (remaining > 0) {
            val toRead = minOf(remaining, buf.size.toLong()).toInt()
            val read = input.read(buf, 0, toRead)
            if (read == -1) break
            remaining -= read
        }
        // Padding for the entry
        val padding = (512 - (count % 512)) % 512
        if (padding > 0 && count > 0) {
            var padRemaining = padding
            while (padRemaining > 0) {
                val toRead = minOf(padRemaining, buf.size.toLong()).toInt()
                val read = input.read(buf, 0, toRead)
                if (read == -1) break
                padRemaining -= read
            }
        }
    }

    /**
     * Computes the SHA-256 hash of a file as a lowercase hex string.
     */
    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

/**
 * Exception thrown when an image download fails due to an HTTP or network error.
 */
class ImageDownloadException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * Exception thrown when the downloaded image fails SHA-256 verification.
 */
class ImageVerificationException(message: String) :
    RuntimeException(message)
