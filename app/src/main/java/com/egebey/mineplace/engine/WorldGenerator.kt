package com.egebey.mineplace.engine

import kotlin.math.sin
import kotlin.random.Random

enum class BiomeType(val label: String, val baseTempC: Float, val weatherEvent: String) {
    ANCIENT_FOREST("Ancient Overgrown Woods", 21f, "Acid Rain"),
    FROZEN_TUNDRA("Glacial Tundra", -14f, "Freezing Blizzard"),
    SCORCHED_DUNES("Scorched Canyons", 42f, "Sandstorm"),
    SKY_ARCHIPELAGO("Aether Sky Ruins", 12f, "Ion Tempest")
}

object WorldGenerator {
    fun generateWorld(engine: VoxelEngine, meta: WorldMeta) {
        val rng = Random(meta.seed)
        engine.activeBiome = meta.biome
        engine.entities.clear()

        for (x in 0 until engine.width) {
            val surfaceY = (24 + sin(x * 0.12 + meta.seed % 10) * 4).toInt()
            for (y in 0 until engine.height) {
                when {
                    y < surfaceY -> engine.setBlock(x, y, BlockType.AIR, false)
                    y == surfaceY -> engine.setBlock(x, y, BlockType.GRASS, false)
                    y < surfaceY + 4 -> engine.setBlock(x, y, BlockType.DIRT, false)
                    else -> engine.setBlock(x, y, if (rng.nextFloat() < 0.06f) BlockType.ORE_CRYSTAL else BlockType.STONE, false)
                }
            }
            if (x % 7 == 0 && x in 6..88) {
                val treeBase = (24 + sin(x * 0.12 + meta.seed % 10) * 4).toInt() - 1
                for (h in 0..3) engine.setBlock(x, treeBase - h, BlockType.WOOD, false)
                for (lx in -1..1) for (ly in -5..-4) engine.setBlock(x + lx, treeBase + ly, BlockType.LEAVES, false)
            }
        }
        // Floating Islands & Ruins
        for (dx in -5..5) {
            engine.setBlock(32 + dx, 9, BlockType.GRASS, false)
            engine.setBlock(72 + dx, 8, BlockType.GRASS, false)
        }
        engine.setBlock(32, 11, BlockType.ANTI_GRAV, false)
        engine.setBlock(72, 10, BlockType.ANTI_GRAV, false)

        engine.entities.addAll(EntityAI.spawnInitialEcosystem(meta.biome))
        engine.playerX = 18f
        engine.playerY = 18f
    }
}
