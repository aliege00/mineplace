package com.egebey.mineplace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egebey.mineplace.engine.*
import com.egebey.mineplace.systems.GraphicsConfig
import com.egebey.mineplace.systems.ShaderPreset

enum class ScreenTab(val title: String) {
    LAUNCHER("LAUNCHER"),
    SHADERS("VIDEO & SHADERS"),
    PLAY("PLAY LIVE")
}

@Composable
fun MineplaceApp() {
    val engine = remember { VoxelEngine() }
    val graphics = remember { GraphicsConfig() }
    var currentTab by remember { mutableStateOf(ScreenTab.LAUNCHER) }

    LaunchedEffect(Unit) {
        WorldGenerator.generateWorld(engine, WorldMeta("Aether Alpha", 40921L, BiomeType.ANCIENT_FOREST))
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0D12))) {
        if (currentTab != ScreenTab.PLAY) {
            Column(
                modifier = Modifier.width(175.dp).fillMaxHeight().background(Color(0xFF10151E)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("MINEPLACE", color = Color(0xFF00E676), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("VOXEL ENGINE v2", color = Color.Gray, fontSize = 10.sp)
                Spacer(Modifier.height(8.dp))
                ScreenTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    Surface(
                        color = if (selected) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().clickable { currentTab = tab }
                    ) {
                        Text(
                            tab.title,
                            color = if (selected) Color(0xFF00E676) else Color.LightGray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (currentTab) {
                ScreenTab.LAUNCHER -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MINEPLACE // SHADER EDITION", color = Color(0xFF00E5FF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Active Shader: ${graphics.selectedPreset.label}", color = Color.Gray)
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { currentTab = ScreenTab.PLAY },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Text("LAUNCH WORLD", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ScreenTab.SHADERS -> VideoSettingsView(graphics)
                ScreenTab.PLAY -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GameCanvas(engine = engine, graphics = graphics, modifier = Modifier.fillMaxSize())
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xAA0A0D12)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "FPS: ${engine.currentFps} | ${graphics.selectedPreset.label} | Temp: ${"%.1f".format(engine.survival.bodyTempC)}°C",
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { currentTab = ScreenTab.LAUNCHER }) { Text("HUB", fontSize = 11.sp) }
                        }
                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xAA0A0D12)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(onClick = { engine.movePlayer(-1.2f, false) }) { Text("<") }
                                Button(onClick = { engine.movePlayer(1.2f, false) }) { Text(">") }
                                Button(onClick = { engine.movePlayer(0f, true) }) { Text("JUMP") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(BlockType.WOOD, BlockType.STONE, BlockType.ORE_CRYSTAL, BlockType.ANTI_GRAV, BlockType.LOGIC_GATE).forEach { block ->
                                    FilterChip(
                                        selected = engine.selectedBlock == block,
                                        onClick = { engine.selectedBlock = block },
                                        label = { Text(block.name, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoSettingsView(graphics: GraphicsConfig) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("VIDEO SETTINGS & SHADERS PIPELINE", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131922)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Shader Preset: ${graphics.selectedPreset.label}", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShaderPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = graphics.selectedPreset == preset,
                            onClick = { graphics.selectedPreset = preset },
                            label = { Text(preset.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF131922)), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Lighting & Frame Optimization", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Volumetric Bevels & Player Aura", fontSize = 13.sp)
                    Switch(checked = graphics.volumetricLighting, onCheckedChange = { graphics.volumetricLighting = it })
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("High-FPS VSync Lock", fontSize = 13.sp)
                    Switch(checked = graphics.targetFps60, onCheckedChange = { graphics.targetFps60 = it })
                }
            }
        }
    }
}
