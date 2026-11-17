package com.egebey.mineplace.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.egebey.mineplace.engine.BlockType
import com.egebey.mineplace.engine.VoxelEngine
import com.egebey.mineplace.systems.GraphicsConfig
import com.egebey.mineplace.systems.ShaderPreset
import kotlin.math.max
import kotlin.math.min

@Composable
fun GameCanvas(engine: VoxelEngine, graphics: GraphicsConfig, modifier: Modifier = Modifier) {
    var frameTrigger by remember { mutableLongStateOf(0L) }

    LaunchedEffect(engine) {
        var lastNanos = withFrameNanos { it }
        var frames = 0
        var fpsTimer = 0L
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastNanos = now
            engine.update(dt)
            frameTrigger = now
            frames++
            fpsTimer += (dt * 1000).toLong()
            if (fpsTimer >= 1000L) {
                engine.currentFps = frames
                frames = 0
                fpsTimer = 0L
            }
        }
    }

    val tileSize = 36f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val camX = (engine.playerX * tileSize - size.width / 2f).coerceIn(0f, max(0f, engine.width * tileSize - size.width))
                    val camY = (engine.playerY * tileSize - size.height / 2f).coerceIn(0f, max(0f, engine.height * tileSize - size.height))
                    engine.mineOrPlaceAt(((tap.x + camX) / tileSize).toInt(), ((tap.y + camY) / tileSize).toInt())
                }
            }
    ) {
        if (frameTrigger < 0L) return@Canvas

        // Atmospheric Gradient Sky Shader
        val skyColors = if (engine.survival.isNight) {
            listOf(Color(0xFF03050A), Color(0xFF0B132B))
        } else {
            listOf(Color(0xFF0F1B2C), Color(0xFF1D3557))
        }
        drawRect(brush = Brush.verticalGradient(skyColors), size = size)

        val camX = (engine.playerX * tileSize - size.width / 2f).coerceIn(0f, max(0f, engine.width * tileSize - size.width))
        val camY = (engine.playerY * tileSize - size.height / 2f).coerceIn(0f, max(0f, engine.height * tileSize - size.height))

        val startCol = max(0, (camX / tileSize).toInt())
        val endCol = min(engine.width - 1, ((camX + size.width) / tileSize).toInt() + 1)
        val startRow = max(0, (camY / tileSize).toInt())
        val endRow = min(engine.height - 1, ((camY + size.height) / tileSize).toInt() + 1)

        for (y in startRow..endRow) {
            for (x in startCol..endCol) {
                val b = engine.getBlock(x, y)
                if (b != BlockType.AIR) {
                    val left = x * tileSize - camX
                    val top = y * tileSize - camY
                    drawRect(
                        color = Color(b.colorHex),
                        topLeft = Offset(left, top),
                        size = Size(tileSize - 1f, tileSize - 1f)
                    )
                    // Top bevel highlight for depth
                    if (graphics.volumetricLighting) {
                        drawRect(
                            color = Color(0x22FFFFFF),
                            topLeft = Offset(left, top),
                            size = Size(tileSize - 1f, 4f)
                        )
                    }
                    // Neon Bloom Glow Shader
                    if (graphics.selectedPreset.bloomGlow &&
                        (b == BlockType.ORE_CRYSTAL || b == BlockType.ANTI_GRAV || b == BlockType.LOGIC_GATE || b == BlockType.LAVA)) {
                        drawRect(
                            color = Color(0x4400FFFF),
                            topLeft = Offset(left - 3f, top - 3f),
                            size = Size(tileSize + 5f, tileSize + 5f)
                        )
                    }
                }
            }
        }

        // Living Entities
        for (e in engine.entities) {
            drawRect(
                color = Color(e.colorHex),
                topLeft = Offset(e.x * tileSize - camX, e.y * tileSize - camY),
                size = Size(tileSize * 0.9f, tileSize * 0.9f)
            )
        }

        // Player with subtle aura
        val px = engine.playerX * tileSize - camX
        val py = engine.playerY * tileSize - camY
        if (graphics.volumetricLighting) {
            drawCircle(
                color = Color(0x2200E676),
                radius = tileSize * 1.8f,
                center = Offset(px + tileSize * 0.4f, py + tileSize * 0.7f)
            )
        }
        drawRect(
            color = Color(0xFF00E676),
            topLeft = Offset(px, py),
            size = Size(tileSize * 0.85f, tileSize * 1.4f)
        )

        // CRT Scanlines Shader
        if (graphics.selectedPreset.scanlines) {
            var scanY = 0f
            while (scanY < size.height) {
                drawRect(color = Color(0x18000000), topLeft = Offset(0f, scanY), size = Size(size.width, 2f))
                scanY += 6f
            }
        }

        if (engine.survival.isNight) {
            drawRect(color = Color(0x6600000A), size = size)
        }
    }
}
