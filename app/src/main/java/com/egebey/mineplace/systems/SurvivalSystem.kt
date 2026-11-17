package com.egebey.mineplace.systems

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.egebey.mineplace.engine.BiomeType

class SurvivalSystem {
    var bodyTempC by mutableFloatStateOf(37.0f)
    var stamina by mutableFloatStateOf(100f)
    var timeOfDay by mutableFloatStateOf(0.25f)
    var activeWeather by mutableStateOf("Clear")
    var sheltered by mutableStateOf(false)
    val isNight: Boolean get() = timeOfDay > 0.70f

    fun update(dt: Float, biome: BiomeType, isSheltered: Boolean) {
        sheltered = isSheltered
        timeOfDay = (timeOfDay + dt * 0.015f) % 1.0f
        activeWeather = if (timeOfDay in 0.45f..0.68f) biome.weatherEvent else "Clear"
        val targetTemp = when {
            isSheltered -> 36.8f
            biome == BiomeType.FROZEN_TUNDRA -> 33.2f
            biome == BiomeType.SCORCHED_DUNES -> 40.5f
            else -> 36.6f
        }
        bodyTempC += (targetTemp - bodyTempC) * dt * 0.25f
        stamina = (stamina + dt * 4f).coerceIn(0f, 100f)
    }
    fun consumeStamina(amount: Float) { stamina = (stamina - amount).coerceIn(0f, 100f) }
}
