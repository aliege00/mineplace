package com.egebey.mineplace.systems

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class ShaderPreset(val label: String, val bloomGlow: Boolean, val scanlines: Boolean) {
    RETRO_CLASSIC("Retro Flat", false, false),
    NEON_AETHER("Neon Aether Shaders", true, false),
    CYBER_CRT("Cyberpunk CRT", true, true)
}

enum class RenderDistance(val label: String, val chunkCols: Int) {
    COMPACT("Compact (64 cols)", 64),
    EXTENDED("Extended (96 cols)", 96),
    ULTRA("Ultra (128 cols)", 128)
}

class GraphicsConfig {
    var selectedPreset by mutableStateOf(ShaderPreset.NEON_AETHER)
    var renderDistance by mutableStateOf(RenderDistance.EXTENDED)
    var targetFps60 by mutableStateOf(true)
    var volumetricLighting by mutableStateOf(true)
}
