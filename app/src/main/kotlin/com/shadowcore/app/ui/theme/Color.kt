package com.shadowcore.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand Colors ──────────────────────────────────────────────────────
val Purple80 = Color(0xFFB388FF)
val Purple60 = Color(0xFF7C4DFF)
val Purple40 = Color(0xFF651FFF)
val Purple20 = Color(0xFF4A148C)

val Violet80 = Color(0xFFCE93D8)
val Violet40 = Color(0xFF9C27B0)

// ── Surface Colors ────────────────────────────────────────────────────
val SurfaceDark = Color(0xFF0F0F1A)
val SurfaceDarkElevated = Color(0xFF1A1A2E)
val SurfaceDarkCard = Color(0xFF1E1E35)
val SurfaceLight = Color(0xFFF8F6FF)
val SurfaceLightCard = Color(0xFFFFFFFF)

// ── Semantic VE State Colors ─────────────────────────────────────────
val VeRunning = Color(0xFF00E676)
val VeRunningDim = Color(0xFF004D2A)
val VePaused = Color(0xFFFFAB40)
val VePausedDim = Color(0xFF5C3D00)
val VeStopped = Color(0xFF78909C)
val VeError = Color(0xFFFF5252)
val VeDownloading = Color(0xFF40C4FF)

// Backward compat aliases (will be removed in next cleanup)
@Deprecated("Use VeRunning", ReplaceWith("VeRunning")) val VmRunning = VeRunning
@Deprecated("Use VeRunningDim", ReplaceWith("VeRunningDim")) val VmRunningDim = VeRunningDim
@Deprecated("Use VePaused", ReplaceWith("VePaused")) val VmPaused = VePaused
@Deprecated("Use VePausedDim", ReplaceWith("VePausedDim")) val VmPausedDim = VePausedDim
@Deprecated("Use VeStopped", ReplaceWith("VeStopped")) val VmStopped = VeStopped
@Deprecated("Use VeError", ReplaceWith("VeError")) val VmError = VeError
@Deprecated("Use VeDownloading", ReplaceWith("VeDownloading")) val VmDownloading = VeDownloading

// ── Capability Tier Colors ────────────────────────────────────────────
val TierUnsupported = Color(0xFF616161)
val TierContainer = Color(0xFF26A69A)
val TierAvf = Color(0xFF42A5F5)
val TierHardware = Color(0xFFAB47BC)

// ── Category Gradient Colors ──────────────────────────────────────────
val WorkStart = Color(0xFF1A73E8)
val WorkEnd = Color(0xFF4FC3F7)
val GamingStart = Color(0xFFE91E63)
val GamingEnd = Color(0xFFFF6090)
val PrivacyStart = Color(0xFF00897B)
val PrivacyEnd = Color(0xFF4DB6AC)
val PersonalStart = Color(0xFF7C4DFF)
val PersonalEnd = Color(0xFFB388FF)
val CustomStart = Color(0xFFFF6D00)
val CustomEnd = Color(0xFFFFAB40)
