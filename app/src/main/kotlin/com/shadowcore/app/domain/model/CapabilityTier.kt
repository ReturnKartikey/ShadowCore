package com.shadowcore.app.domain.model

/**
 * Device capability tiers for virtualization support.
 *
 * Tier 0 → Unsupported: Device cannot run any form of virtualization
 * Tier 1 → Container Mode: Lightweight OS-level containers (namespaces/chroot)
 * Tier 2 → AVF Supported: Android Virtualization Framework with Microdroid
 * Tier 3 → Hardware Accelerated: Full KVM with GPU passthrough & Vulkan
 */
enum class CapabilityTier(
    val level: Int,
    val displayName: String,
    val description: String,
    val maxSimultaneousVms: Int,
    val supportsGpuPassthrough: Boolean,
    val supportsSnapshot: Boolean,
) {
    TIER_0_UNSUPPORTED(
        level = 0,
        displayName = "Unsupported",
        description = "This device does not support virtualization",
        maxSimultaneousVms = 0,
        supportsGpuPassthrough = false,
        supportsSnapshot = false,
    ),

    TIER_1_CONTAINER(
        level = 1,
        displayName = "Container Mode",
        description = "Lightweight isolation using OS containers",
        maxSimultaneousVms = 2,
        supportsGpuPassthrough = false,
        supportsSnapshot = true,
    ),

    TIER_2_AVF(
        level = 2,
        displayName = "AVF Virtualization",
        description = "Android Virtualization Framework supported",
        maxSimultaneousVms = 3,
        supportsGpuPassthrough = false,
        supportsSnapshot = true,
    ),

    TIER_3_HARDWARE_ACCELERATED(
        level = 3,
        displayName = "Hardware Accelerated",
        description = "Full KVM with GPU passthrough",
        maxSimultaneousVms = 5,
        supportsGpuPassthrough = true,
        supportsSnapshot = true,
    );

    val isSupported: Boolean get() = level > 0
    val isFullVirtualization: Boolean get() = level >= 2
    val isAccelerated: Boolean get() = level >= 3
}
