package com.egebey.mineplace.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.egebey.mineplace.engine.BlockType
import com.egebey.mineplace.engine.VoxelEngine
import kotlin.math.max
import kotlin.math.min

@Composable
fun GameCanvas(engine: VoxelEngine, modifier: Modifier = Modifier) {
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
            if (fpsTimer >= 1000L) { engine.currentFps = frames; frames = 0; fpsTimer = 0L }
        }
    }
    val tileSize = 34f
    Canvas(modifier = modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures { tap ->
            val camX = (engine.playerX * tileSize - size.width / 2f).coerceIn(0f, max(0f, engine.width * tileSize - size.width))
            val camY = (engine.playerY * tileSize - size.height / 2f).coerceIn(0f, max(0f, engine.height * tileSize - size.height))
            engine.mineOrPlaceAt(((tap.x + camX) / tileSize).toInt(), ((tap.y + camY) / tileSize).toInt())
        }
    }) {
        if (frameTrigger < 0L) return@Canvas
        drawRect(color = if (engine.survival.isNight) Color(0xFF060913) else Color(0xFF152638), size = size)
        val camX = (engine.playerX * tileSize - size.width / 2f).coerceIn(0f, max(0f, engine.width * tileSize - size.width))
        val camY = (engine.playerY * tileSize - size.height / 2f).coerceIn(0f, max(0f, engine.height * tileSize - size.height))

        for (y in max(0, (camY / tileSize).toInt())..min(engine.height - 1, ((camY + size.height) / tileSize).toInt() + 1)) {
            for (x in max(0, (camX / tileSize).toInt())..min(engine.width - 1, ((camX + size.width) / tileSize).toInt() + 1)) {
                val b = engine.getBlock(x, y)
                if (b != BlockType.AIR) drawRect(color = Color(b.colorHex), topLeft = Offset(x * tileSize - camX, y * tileSize - camY), size = Size(tileSize - 1f, tileSize - 1f))
            }
        }
        for (e in engine.entities) {
            drawRect(color = Color(e.colorHex), topLeft = Offset(e.x * tileSize - camX, e.y * tileSize - camY), size = Size(tileSize * 0.9f, tileSize * 0.9f))
        }
        drawRect(color = Color(0xFF00E676), topLeft = Offset(engine.playerX * tileSize - camX, engine.playerY * tileSize - camY), size = Size(tileSize * 0.85f, tileSize * 1.4f))
    }
}
