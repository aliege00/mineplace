package com.egebey.mineplace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egebey.mineplace.engine.*

@Composable
fun MineplaceApp() {
    val engine = remember { VoxelEngine() }
    var playing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { WorldGenerator.generateWorld(engine, WorldMeta("Aether Alpha", 40921L, BiomeType.ANCIENT_FOREST)) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0D12))) {
        if (!playing) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MINEPLACE", color = Color(0xFF00E676), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { playing = true }) { Text("LAUNCH GAME") }
            }
        } else {
            GameCanvas(engine = engine, modifier = Modifier.fillMaxSize())
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xAA0A0D12)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("FPS: ${engine.currentFps} | Temp: ${"%.1f".format(engine.survival.bodyTempC)}°C", color = Color(0xFF00E676))
                Button(onClick = { playing = false }) { Text("EXIT") }
            }
            Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xAA0A0D12)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    Button(onClick = { engine.movePlayer(-1f, false) }) { Text("<") }
                    Button(onClick = { engine.movePlayer(1f, false) }) { Text(">") }
                    Button(onClick = { engine.movePlayer(0f, true) }) { Text("JUMP") }
                }
            }
        }
    }
}
