package com.egebey.mineplace.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.egebey.mineplace.systems.BlueprintModSystem
import com.egebey.mineplace.systems.SurvivalSystem
import kotlin.math.max
import kotlin.math.min

enum class BlockType(val id: Int, val colorHex: Long, val isSolid: Boolean, val supportStrength: Int, val isFluid: Boolean = false) {
    AIR(0, 0x00000000, false, 0),
    GRASS(1, 0xFF388E3C, true, 4),
    DIRT(2, 0xFF5D4037, true, 3),
    STONE(3, 0xFF616161, true, 8),
    WOOD(4, 0xFF8D6E63, true, 5),
    LEAVES(5, 0xFF2E7D32, true, 1),
    ORE_CRYSTAL(6, 0xFF00E5FF, true, 8),
    ANTI_GRAV(7, 0xFFD500F9, true, 99),
    RUIN_BRICK(8, 0xFF455A64, true, 10),
    WATER(9, 0xAA0288D1, false, 0, true),
    LAVA(10, 0xEEFF3D00, false, 0, true),
    STEAM_ENGINE(11, 0xFFFFB300, true, 6),
    LOGIC_GATE(12, 0xFFFF1744, true, 6)
}

data class WorldMeta(val name: String, val seed: Long, val biome: BiomeType, val playtimeMinutes: Int = 0)

class VoxelEngine(val width: Int = 96, val height: Int = 48) {
    val grid = IntArray(width * height)
    val fluidLevel = ByteArray(width * height)

    var playerX by mutableFloatStateOf(16f)
    var playerY by mutableFloatStateOf(18f)
    var velocityY by mutableFloatStateOf(0f)
    var isGrounded by mutableStateOf(false)

    var selectedBlock by mutableStateOf(BlockType.WOOD)
    var treesChopped by mutableIntStateOf(0)
    var blocksMined by mutableIntStateOf(0)
    var blocksPlaced by mutableIntStateOf(0)
    var currentFps by mutableIntStateOf(60)

    var activeBiome by mutableStateOf(BiomeType.ANCIENT_FOREST)
    var eventBanner by mutableStateOf("World Loaded: Explore Ruined Civilizations & Floating Isles")

    val entities = mutableStateListOf<LivingEntity>()
    val survival = SurvivalSystem()
    val blueprints = BlueprintModSystem()

    private var physicsAccumulator = 0f
    private var fluidTickCounter = 0

    fun getBlock(x: Int, y: Int): BlockType {
        if (x !in 0 until width || y !in 0 until height) return BlockType.STONE
        val id = grid[y * width + x]
        return BlockType.entries.firstOrNull { it.id == id } ?: BlockType.AIR
    }

    fun setBlock(x: Int, y: Int, type: BlockType, checkPhysics: Boolean = true) {
        if (x !in 0 until width || y !in 0 until height) return
        grid[y * width + x] = type.id
        if (type.isFluid) fluidLevel[y * width + x] = 8 else fluidLevel[y * width + x] = 0
        if (checkPhysics) enforceStructuralIntegrity(x, y)
    }

    private fun enforceStructuralIntegrity(cx: Int, cy: Int) {
        for (y in max(1, cy - 5)..min(height - 2, cy + 5)) {
            for (x in max(1, cx - 5)..min(width - 2, cx + 5)) {
                val block = getBlock(x, y)
                if (block == BlockType.AIR || block.isFluid || block == BlockType.ANTI_GRAV) continue
                val below = getBlock(x, y + 1)
                val left = getBlock(x - 1, y)
                val right = getBlock(x + 1, y)
                val hasSupport = below.isSolid || (left.isSolid && left.supportStrength >= 4) || (right.isSolid && right.supportStrength >= 4) || hasNearbyAntiGrav(x, y)
                if (!hasSupport && y < height - 2) {
                    grid[y * width + x] = BlockType.AIR.id
                    grid[(y + 1) * width + x] = block.id
                }
            }
        }
    }

    private fun hasNearbyAntiGrav(x: Int, y: Int): Boolean {
        for (dy in -4..4) for (dx in -4..4) {
            if (getBlock(x + dx, y + dy) == BlockType.ANTI_GRAV) return true
        }
        return false
    }

    fun mineOrPlaceAt(wx: Int, wy: Int) {
        if (wx !in 0 until width || wy !in 0 until height) return
        val existing = getBlock(wx, wy)
        if (existing != BlockType.AIR && !existing.isFluid) {
            if (existing == BlockType.WOOD || existing == BlockType.LEAVES) {
                treesChopped++
                if (treesChopped == 8) {
                    eventBanner = "WARNING: Forest Guardian Awakened by Deforestation!"
                    entities.add(EntityAI.spawnForestGuardian(playerX + 8f, playerY - 2f))
                }
            }
            blocksMined++
            setBlock(wx, wy, BlockType.AIR)
        } else {
            blocksPlaced++
            setBlock(wx, wy, selectedBlock)
            blueprints.evaluateCircuitAt(this, wx, wy)
        }
    }

    fun movePlayer(dx: Float, jump: Boolean) {
        val nextX = (playerX + dx).coerceIn(1f, (width - 2).toFloat())
        if (!getBlock(nextX.toInt(), playerY.toInt()).isSolid) {
            playerX = nextX
            survival.consumeStamina(0.15f)
        }
        if (jump && isGrounded) {
            velocityY = -11.5f
            isGrounded = false
            survival.consumeStamina(1.2f)
        }
    }

    fun update(dt: Float) {
        val clampedDt = dt.coerceAtMost(0.05f)
        physicsAccumulator += clampedDt
        velocityY += 28f * clampedDt
        val nextY = playerY + velocityY * clampedDt
        val footBlock = getBlock(playerX.toInt(), (nextY + 0.9f).toInt())
        if (footBlock.isSolid && velocityY >= 0f) {
            playerY = (nextY + 0.9f).toInt() - 0.91f
            velocityY = 0f
            isGrounded = true
        } else {
            playerY = nextY.coerceIn(1f, (height - 2).toFloat())
            isGrounded = false
        }
        val isSheltered = checkWeatherShelter(playerX.toInt(), playerY.toInt())
        survival.update(clampedDt, activeBiome, isSheltered)
        EntityAI.updateEntities(this, clampedDt)
        fluidTickCounter++
        if (fluidTickCounter % 6 == 0) {
            for (y in height - 2 downTo 1) for (x in 1 until width - 1) {
                val b = getBlock(x, y)
                if (b == BlockType.WATER || b == BlockType.LAVA) {
                    if (getBlock(x, y + 1) == BlockType.AIR) setBlock(x, y + 1, b, checkPhysics = false)
                    else if (getBlock(x - 1, y) == BlockType.AIR && (x + y) % 2 == 0) setBlock(x - 1, y, b, checkPhysics = false)
                    else if (getBlock(x + 1, y) == BlockType.AIR) setBlock(x + 1, y, b, checkPhysics = false)
                }
            }
        }
    }

    private fun checkWeatherShelter(px: Int, py: Int): Boolean {
        for (dy in 1..4) if (getBlock(px, py - dy).isSolid) return true
        return false
    }
}
