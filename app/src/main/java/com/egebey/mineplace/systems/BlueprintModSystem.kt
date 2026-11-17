package com.egebey.mineplace.systems

import androidx.compose.runtime.mutableStateListOf
import com.egebey.mineplace.engine.BlockType
import com.egebey.mineplace.engine.VoxelEngine

data class ModPack(val id: String, val name: String, val category: String, val description: String, var enabled: Boolean)

class BlueprintModSystem {
    val installedMods = mutableStateListOf(
        ModPack("mod_steam", "Steam & Logic", "Mechanics", "Enables Steam Engines & Logic Gates", true),
        ModPack("mod_fauna", "Apex Biome+", "AI & Mobs", "Accelerates fauna adaptation & raids", true)
    )
    fun evaluateCircuitAt(engine: VoxelEngine, x: Int, y: Int) {
        if (engine.getBlock(x, y) == BlockType.LOGIC_GATE) {
            engine.eventBanner = "BLUEPRINT ACTIVE: Steam-Logic Thruster Linked!"
        }
    }
}
