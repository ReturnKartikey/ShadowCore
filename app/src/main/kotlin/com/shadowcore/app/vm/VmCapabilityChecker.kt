package com.shadowcore.app.vm

import android.content.Context
import android.os.Build
import com.shadowcore.app.domain.model.CapabilityTier
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects the device's virtualization capability tier.
 *
 * Tier 0 → Unsupported: Cannot run any form of virtualization
 * Tier 1 → Container Mode: OS-level containers (namespaces/chroot)
 * Tier 2 → AVF Supported: VirtualMachineManager available
 * Tier 3 → Hardware Accelerated: Full KVM with GPU passthrough
 */
@Singleton
class VmCapabilityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Cached capability tier — computed once on first access.
     */
    val capabilityTier: CapabilityTier by lazy { detectCapabilityTier() }

    /**
     * Detailed capability report for settings/debug screen.
     */
    val capabilityReport: CapabilityReport by lazy { generateReport() }

    private fun detectCapabilityTier(): CapabilityTier {
        // Tier 3: Check for hardware-accelerated KVM with GPU passthrough
        if (hasKvmSupport() && hasGpuPassthrough() && isAvfAvailable()) {
            return CapabilityTier.TIER_3_HARDWARE_ACCELERATED
        }

        // Tier 2: Check for AVF (VirtualMachineManager) availability
        if (isAvfAvailable()) {
            return CapabilityTier.TIER_2_AVF
        }

        // Tier 1: Container mode — available on Android 10+ (our minSdk)
        if (hasContainerSupport()) {
            return CapabilityTier.TIER_1_CONTAINER
        }

        // Tier 0: Unsupported
        return CapabilityTier.TIER_0_UNSUPPORTED
    }

    /**
     * Check if /dev/kvm is accessible (KVM kernel module loaded).
     */
    private fun hasKvmSupport(): Boolean {
        return try {
            val kvmDevice = File("/dev/kvm")
            kvmDevice.exists() && kvmDevice.canRead()
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * Check if Android Virtualization Framework is available.
     * VirtualMachineManager was introduced in Android 14 (API 34).
     */
    private fun isAvfAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < 34) return false
        return try {
            val vmManagerClass = Class.forName(
                "android.system.virtualmachine.VirtualMachineManager"
            )
            val systemService = context.getSystemService(vmManagerClass)
            systemService != null
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Check if GPU passthrough / Vulkan inside VM is supported.
     * Safely handles missing EGL context — no GLES calls without a context.
     */
    private fun hasGpuPassthrough(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < 34) return false
            // Read GPU EGL implementation via getprop command
            // SystemProperties is a hidden API, so we use Runtime.exec instead
            val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.hardware.egl"))
            val renderer = process.inputStream.bufferedReader().readLine()?.trim() ?: ""
            process.waitFor()

            val hasVirtioGpu = renderer.contains("virtio", ignoreCase = true) ||
                renderer.contains("swiftshader", ignoreCase = true)

            hasVirtioGpu && hasKvmSupport()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Check container mode support — available on all Android 10+ devices.
     */
    private fun hasContainerSupport(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * Gets available system RAM in MB.
     */
    fun getAvailableRamMb(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
            as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.availMem / (1024 * 1024)).toInt()
    }

    /**
     * Gets total system RAM in MB.
     */
    fun getTotalRamMb(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
            as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.totalMem / (1024 * 1024)).toInt()
    }

    /**
     * Gets available internal storage in MB.
     */
    fun getAvailableStorageMb(): Long {
        val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        return stat.availableBytes / (1024 * 1024)
    }

    private fun generateReport(): CapabilityReport {
        return CapabilityReport(
            tier = capabilityTier,
            androidVersion = Build.VERSION.SDK_INT,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            hasKvm = hasKvmSupport(),
            hasAvf = isAvfAvailable(),
            hasGpuPassthrough = hasGpuPassthrough(),
            totalRamMb = getTotalRamMb(),
            availableRamMb = getAvailableRamMb(),
            availableStorageMb = getAvailableStorageMb(),
        )
    }
}

/**
 * Detailed report of device virtualization capabilities.
 */
data class CapabilityReport(
    val tier: CapabilityTier,
    val androidVersion: Int,
    val deviceModel: String,
    val hasKvm: Boolean,
    val hasAvf: Boolean,
    val hasGpuPassthrough: Boolean,
    val totalRamMb: Int,
    val availableRamMb: Int,
    val availableStorageMb: Long,
)
