package com.shadowcore.app.ui.screens.imagemanager

import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowcore.app.engine.ImageManager
import com.shadowcore.app.engine.ImageRegistry
import com.shadowcore.app.engine.ImageStatus
import com.shadowcore.app.engine.SystemImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageItemState(
    val image: SystemImage,
    val status: ImageStatus = ImageStatus.NOT_DOWNLOADED,
    val downloadProgress: Float = 0f,
)

data class ImageManagerUiState(
    val images: List<ImageItemState> = emptyList(),
    val usedStorageMb: Long = 0,
    val availableStorageMb: Long = 0,
    val downloadedCount: Int = 0,
)

@HiltViewModel
class ImageManagerViewModel @Inject constructor(
    private val imageManager: ImageManager,
    private val imageRegistry: ImageRegistry,
) : ViewModel() {

    private val _state = MutableStateFlow(ImageManagerUiState())
    val state: StateFlow<ImageManagerUiState> = _state.asStateFlow()

    init {
        loadImages()
    }

    private fun loadImages() {
        val downloadedVersions = imageManager.getDownloadedImages()

        val imageStates = imageRegistry.availableImages.map { image ->
            ImageItemState(
                image = image,
                status = if (image.version in downloadedVersions) ImageStatus.READY
                         else ImageStatus.NOT_DOWNLOADED,
            )
        }

        val usedMb = downloadedVersions.sumOf { version ->
            imageRegistry.getImage(version)?.downloadSizeMb?.toLong() ?: 0L
        }

        _state.update {
            it.copy(
                images = imageStates,
                usedStorageMb = usedMb,
                availableStorageMb = getAvailableStorageMb(),
                downloadedCount = downloadedVersions.size,
            )
        }
    }

    fun downloadImage(image: SystemImage) {
        viewModelScope.launch {
            imageManager.downloadImage(image).collect { progress ->
                _state.update { state ->
                    state.copy(
                        images = state.images.map { item ->
                            if (item.image.version == image.version) {
                                item.copy(
                                    status = progress.status,
                                    downloadProgress = progress.progressPercent,
                                )
                            } else item
                        }
                    )
                }
            }

            // Refresh after download completes
            loadImages()
        }
    }

    fun deleteImage(version: String) {
        imageManager.deleteImage(version)
        loadImages()
    }

    fun cancelDownload(version: String) {
        // TODO: Cancel in-flight download
        _state.update { state ->
            state.copy(
                images = state.images.map { item ->
                    if (item.image.version == version) {
                        item.copy(status = ImageStatus.NOT_DOWNLOADED, downloadProgress = 0f)
                    } else item
                }
            )
        }
    }

    private fun getAvailableStorageMb(): Long {
        return try {
            val stat = StatFs(android.os.Environment.getDataDirectory().path)
            stat.availableBytes / (1024 * 1024)
        } catch (_: Exception) {
            0L
        }
    }
}
