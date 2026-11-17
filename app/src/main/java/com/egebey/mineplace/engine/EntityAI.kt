package com.egebey.mineplace.engine

import kotlin.math.abs
import kotlin.math.sign

enum class EntityRole { EVOLVING_FAUNA, SETTLER_NPC, DYNAMIC_BOSS, UTILITY_PET }

data class LivingEntity(
    val id: String, val name: String, val role: EntityRole,
    var x: Float, var y: Float, val colorHex: Long,
    var adaptationTrait: String, var statusNote: String
)

object EntityAI {
    fun spawnInitialEcosystem(biome: BiomeType): List<LivingEntity> = listOf(
        LivingEntity("fauna_1", "Adapted Fox", EntityRole.EVOLVING_FAUNA, 24f, 20f, 0xFFFF7043, "Thick Coat", "Adapted to ${biome.label}"),
        LivingEntity("settler_1", "Ranger Kael", EntityRole.SETTLER_NPC, 38f, 20f, 0xFF42A5F5, "Builder", "Trades Crystal | Defending Camp"),
        LivingEntity("pet_hound", "Ore Hound", EntityRole.UTILITY_PET, 15f, 19f, 0xFFAB47BC, "Ore Scent", "Sniffing Crystal veins"),
        LivingEntity("pet_falcon", "Sky Falcon", EntityRole.UTILITY_PET, 17f, 14f, 0xFF26C6DA, "Radar", "Scanning Sky Ruins")
    )

    fun spawnForestGuardian(x: Float, y: Float) =
        LivingEntity("boss_guardian", "Forest Guardian", EntityRole.DYNAMIC_BOSS, x, y, 0xFFFF1744, "Deforestation Trigger", "Hunting player!")

    fun updateEntities(engine: VoxelEngine, dt: Float) {
        val px = engine.playerX
        for (e in engine.entities) {
            when (e.role) {
                EntityRole.UTILITY_PET -> {
                    val dist = px - e.x
                    if (abs(dist) > 2.5f) e.x += sign(dist) * 4.5f * dt
                    e.y = (engine.playerY - if (e.id == "pet_falcon") 3.5f else 0.5f).coerceIn(2f, 44f)
                }
                EntityRole.DYNAMIC_BOSS -> {
                    e.x += sign(px - e.x) * 3.8f * dt
                    e.y = engine.playerY
                }
                EntityRole.SETTLER_NPC -> {
                    e.statusNote = if (engine.survival.isNight) "Defending from Night Raid!" else "Camp Safe"
                }
                EntityRole.EVOLVING_FAUNA -> {
                    e.x = (e.x + 1.2f * dt).let { if (it > engine.width - 4) 10f else it }
                }
            }
        }
    }
}
